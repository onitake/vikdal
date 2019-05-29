package ch.seto.vikdal.dex;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.Adler32;

import ch.seto.vikdal.ProgressListener;
import ch.seto.vikdal.dalvik.IllegalInstructionException;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.Instructions;
import ch.seto.vikdal.java.ClassDescriptor;
import ch.seto.vikdal.java.ClassFieldDescriptor;
import ch.seto.vikdal.java.ClassMethodDescriptor;
import ch.seto.vikdal.java.FieldDescriptor;
import ch.seto.vikdal.java.MethodDescriptor;
import ch.seto.vikdal.java.StaticClassFieldDescriptor;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.TryDescriptor;

public class Dex implements SymbolTable {
	
	private final byte[] DEX_FILE_MAGIC = { 0x64, 0x65, 0x78, 0x0a, 0x30, 0x33, 0x35, 0x00 };
	private final long ENDIAN_CONSTANT = 0x12345678L;
	private final long REVERSE_ENDIAN_CONSTANT = 0x78563412L;
	private final long NO_INDEX = 0xffffffffL;

	private static final class Mapping {
		public long offset;
		public long size;
		public Mapping(long ofs) {
			this(-1, ofs);
		}
		public Mapping(long siz, long ofs) {
			offset = ofs;
			size = siz;
		}
		public String toString() {
			if (size == -1) {
				return String.format("@0x%08x", offset);
			} else {
				return String.format("%d bytes @0x%08x", size, offset);
			}
		}
	}
	
	private static final class ProtoId {
		public long shorty_idx;
		public long return_type_idx;
		public long parameters_off;
		public List<Integer> parameters;
		public ProtoId(long shorty, long type, long off) {
			shorty_idx = shorty;
			return_type_idx = type;
			parameters_off = off;
			parameters = new ArrayList<Integer>();
		}
	}
	
	private static final class FieldId {
		public int class_idx;
		public int type_idx;
		public long name_idx;
		public FieldId(int klass, int type, long name) {
			class_idx = klass;
			type_idx = type;
			name_idx = name;
		}
	}
	
	private static final class MethodId {
		public int class_idx;
		public int proto_idx;
		public long name_idx;
		public MethodId(int klass, int proto, long name) {
			class_idx = klass;
			proto_idx = proto;
			name_idx = name;
		}
	}
	
	private static final class TryDef {
		public long start_addr;
		public int insn_count;
		public int handler_off;
		public Map<Long, Long> catches;
		public long catch_all_addr;
		public TryDef(long start, int count, int off) {
			start_addr = start;
			insn_count = count;
			handler_off = off;
			catches = new LinkedHashMap<Long, Long>();
			catch_all_addr = -1;
		}
	}
	
	private static final class MethodDef {
		public long method_idx;
		public EnumSet<Access> access_flags;
		public long code_off;
		public int registers_size;
		public int ins_size;
		public int outs_size;
		public long debug_info_off;
		public Set<TryDef> exceptions;
		public SortedMap<Integer, Instruction> instructions;
		public MethodDef(long index, EnumSet<Access> access, long code) {
			method_idx = index;
			access_flags = access;
			code_off = code;
			exceptions = new HashSet<TryDef>();
		}
	}
	
	private static final class ClassDef {
		public long class_idx;
		public long access_flags;
		public long superclass_idx;
		public long interfaces_off;
		public long source_file_idx;
		public long annotations_off;
		public long class_data_off;
		public long static_values_off;
		// unordered set of type_idx
		public Set<Integer> interfaces;
		// ordered list of N Values, maps to the first N of M static fields (fields N + 1 to M must be initialized to 0 or null)
		public List<Value> static_values;
		// ordered map from type_idx to an ordered map from name_idx to Value
		public Map<Long, Map<Long, Value>> class_annotations;
		// ordered map from field_idx to an ordered map from type_idx to an ordered map from name_idx to Value
		public Map<Long, Map<Long, Map<Long, Value>>> field_annotations;
		// ordered map from method_idx to an ordered map from type_idx to an ordered map from name_idx to Value
		public Map<Long, Map<Long, Map<Long, Value>>> method_annotations;
		// ordered map from method_idx to ordered list of arguments, each an ordered map from type_idx to an ordered map from name_idx to Value
		public Map<Long, List<Map<Long, Map<Long, Value>>>> parameter_annotations;
		// ordered map from field_idx to access_flags
		public Map<Long, EnumSet<Access>> static_fields;
		// ordered map from field_idx to access_flags
		public Map<Long, EnumSet<Access>> instance_fields;
		// ordered map from method_idx to method definition
		public Map<Long, MethodDef> direct_methods;
		// ordered map from method_idx to method definition
		public Map<Long, MethodDef> virtual_methods;
	}
	
	private Set<ProgressListener> listeners;
	private File dexfile;
	private byte[] dexdata;
	private boolean parsed;
	private float progress;
	
	private DexReader reader;
	private Mapping link;
	private Mapping map;
	private Mapping string_ids;
	private Mapping type_ids;
	private Mapping proto_ids;
	private Mapping field_ids;
	private Mapping method_ids;
	private Mapping class_defs;
	private Mapping data;
	private List<Long> string_id_items;
	private List<Long> type_id_items;
	private List<ProtoId> proto_id_items;
	private List<FieldId> field_id_items;
	private List<MethodId> method_id_items;
	// ordered map from type_idx to class definition
	private Map<Long, ClassDef> class_def_items;
	private byte[] link_data;
	private byte[] data_seg;
	private long file_size;
	private long header_size;
	private long endian_tag;
	private List<String> string_table;
	
	/**
	 * Initializes the dex parser for reading from file, but doesn't parse the contents yet.
	 * Call {@link #parse()} to actually parse the file.
	 * @param file a filename pointing to a DEX file
	 */
	public Dex(String file) {
		this();
		dexfile = new File(file);
	}

	/**
	 * Initializes the dex parser for reading from file, but doesn't parse the contents yet.
	 * Call {@link #parse()} to actually parse the file.
	 * @param file a file object pointing to a DEX file
	 */
	public Dex(File file) {
		this();
		dexfile = file;
	}

	/**
	 * Initializes the dex parser for reading from file, but doesn't parse the contents yet.
	 * Call {@link #parse()} to actually parse the file.
	 * @param data a byte buffer containing the contents of a DEX archive
	 */
	public Dex(byte[] data) {
		this();
		dexdata = data;
	}
	
	private Dex() {
		listeners = new HashSet<ProgressListener>();
		parsed = false;
		progress = 0.0f;
		string_table = new ArrayList<String>();
		string_id_items = new ArrayList<Long>();
		type_id_items = new ArrayList<Long>();
		proto_id_items = new ArrayList<ProtoId>();
		field_id_items = new ArrayList<FieldId>();
		method_id_items = new ArrayList<MethodId>();
		class_def_items = new LinkedHashMap<Long, ClassDef>();
	}
	
	@Override
	public String toString() {
		return "DEX {" + "\n" +
			"\t" + "file_size = " + file_size + "\n" +
			"\t" + "header_size = " + header_size + "\n" +
			"\t" + String.format("endian_tag = 0x%08x", endian_tag) + "\n" +
			"\t" + "link " + link + "\n" +
			"\t" + "map " + map + "\n" +
			"\t" + "string_ids " + string_ids + "\n" +
			"\t" + "type_ids " + type_ids + "\n" +
			"\t" + "proto_ids " + proto_ids + "\n" +
			"\t" + "field_ids " + field_ids + "\n" +
			"\t" + "method_ids " + method_ids + "\n" +
			"\t" + "class_defs " + class_defs + "\n" +
			"\t" + "data " + data + "\n" +
		"}";
	}
	
	/**
	 * Attaches a loading progress listener to the DEX parser.
	 * Listeners can only be attached once.
	 * The listener will receive an immediate update with the current progress.
	 * @param listener a progress listener
	 */
	public void addProgressListener(ProgressListener listener) {
		listeners.add(listener);
		listener.progressUpdated(progress);
	}
	
	/**
	 * Stops the listener from receiving progress updates.
	 * @param listener a progress listener
	 */
	public void removeProgressListener(ProgressListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Starts the parsing process. Progress updates will be sent to the attached listeners periodically.
	 * @throws IOException if the DEX file can't be read or some other form of I/O error occured
	 * @throws DexFormatException if the DEX file can't be parsed correctly
	 */
	public void parse() throws IOException, DexFormatException {
		if (reader != null) {
			reader.close();
		}
		float step = 1.0f / 16;
		float progr = 0.0f;
		updateProgress(progr);
		if (dexfile != null) {
			reader = new DexReader(dexfile);
		} else {
			reader = new DexReader(dexdata);
		}
		try {
			updateProgress(progr += step);
			parseHeader(true);
			updateProgress(progr += step);
			parseStringIds();
			updateProgress(progr += step);
			parseTypeIds();
			updateProgress(progr += step);
			parseProtoIds();
			updateProgress(progr += step);
			parseFieldIds();
			updateProgress(progr += step);
			parseMethodIds();
			updateProgress(progr += step);
			parseClassDefs();
			updateProgress(progr += step);
			parseData();
			updateProgress(progr += step);
			parseLinkData();
			updateProgress(progr += step);
			parseStringTable();
			updateProgress(progr += step);
			parsePrototypeTables();
			updateProgress(progr += step);
			parseClassTables();
			updateProgress(progr += step);
			parseClassData();
			updateProgress(progr += step);
			parseCode();
			updateProgress(progr += step);
			extendStaticFields();
			updateProgress(1.0f);
		} catch (DexEncodingException e) {
			throw new DexFormatException("Encoding error in DEX file", e);
		} catch (IOException e) {
			throw e;
		} finally {
			reader.close();
			reader = null;
		}
		parsed = true;
	}
	
	private void updateProgress(float f) {
		progress = f;
		for (ProgressListener listener : listeners) {
			listener.progressUpdated(progress);
		}
	}

	private void parseHeader(boolean check) throws IOException, DexFormatException {
		byte[] magic = new byte[8];
		reader.read(magic);
		if (!Arrays.equals(magic, DEX_FILE_MAGIC)) throw new DexFormatException("Invalid magic");
		
		long checksum = reader.readLEUnsignedInt();
		byte[] signature = new byte[20];
		reader.read(signature);
		Adler32 adler32 = new Adler32();
		adler32.update(signature);
		MessageDigest sha1 = null;
		try {
			sha1 = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		long headerPos = reader.getFilePointer();
		long available = reader.length() - headerPos;
		byte[] buffer = new byte[4096];
		int len;
		while (available > 0 && (len = reader.read(buffer, 0, (int) available)) != -1) {
			adler32.update(buffer, 0, len);
			if (sha1 != null) {
				sha1.update(buffer, 0, len);
			}
			available -= len;
		}
		if (check) {
			if (adler32.getValue() != checksum) throw new DexFormatException("Invalid Adler32 checksum");
			if (!Arrays.equals(sha1.digest(), signature)) throw new DexFormatException("Invalid SHA1 checksum");
		}
		//System.out.println(String.format("adler32=0x%08x checksum=0x%08x", adler32.getValue(), checksum));
		reader.seek(headerPos);

		file_size = reader.readLEUnsignedInt();
		header_size = reader.readLEUnsignedInt();
		endian_tag = reader.readLEUnsignedInt();
		if (endian_tag == REVERSE_ENDIAN_CONSTANT) {
			System.out.println("Endianness reversed, turning on conversion");
			reader.setBigEndian(true);
		} else if (endian_tag != ENDIAN_CONSTANT) {
			throw new DexFormatException("Invalid endiannes tag");
		}
		link = new Mapping(reader.readUnsignedInt(), reader.readUnsignedInt());
		map = new Mapping(reader.readUnsignedInt());
		string_ids = new Mapping(reader.readUnsignedInt(), reader.readUnsignedInt());
		type_ids = new Mapping(reader.readUnsignedInt(), reader.readUnsignedInt());
		proto_ids = new Mapping(reader.readUnsignedInt(), reader.readUnsignedInt());
		field_ids = new Mapping(reader.readUnsignedInt(), reader.readUnsignedInt());
		method_ids = new Mapping(reader.readUnsignedInt(), reader.readUnsignedInt());
		class_defs = new Mapping(reader.readUnsignedInt(), reader.readUnsignedInt());
		data = new Mapping(reader.readUnsignedInt(), reader.readUnsignedInt());
	}

	private void parseStringIds() throws IOException {
		string_id_items.clear();
		if (string_ids != null) {
			reader.seek(string_ids.offset);
			int items = (int) (string_ids.size);
			for (int i = 0; i < items; i++) {
				string_id_items.add(reader.readUnsignedInt());
			}
			
		}
	}

	private void parseTypeIds() throws IOException {
		type_id_items.clear();
		if (type_ids != null) {
			reader.seek(type_ids.offset);
			int items = (int) (type_ids.size);
			for (int i = 0; i < items; i++) {
				type_id_items.add(reader.readUnsignedInt());
			}
			
		}
	}

	private void parseProtoIds() throws IOException {
		proto_id_items.clear();
		if (proto_ids != null) {
			reader.seek(proto_ids.offset);
			int items = (int) (proto_ids.size);
			for (int i = 0; i < items; i++) {
				proto_id_items.add(new ProtoId(reader.readUnsignedInt(), reader.readUnsignedInt(), reader.readUnsignedInt()));
			}
			
		}
	}

	private void parseFieldIds() throws IOException {
		field_id_items.clear();
		if (field_ids != null) {
			reader.seek(field_ids.offset);
			int items = (int) (field_ids.size);
			for (int i = 0; i < items; i++) {
				field_id_items.add(new FieldId(reader.readUnsignedShort(), reader.readUnsignedShort(), reader.readUnsignedInt()));
			}
			
		}
	}

	private void parseMethodIds() throws IOException {
		method_id_items.clear();
		if (method_ids != null) {
			reader.seek(method_ids.offset);
			int items = (int) (method_ids.size);
			for (int i = 0; i < items; i++) {
				method_id_items.add(new MethodId(reader.readUnsignedShort(), reader.readUnsignedShort(), reader.readUnsignedInt()));
			}
			
		}
	}

	private void parseClassDefs() throws IOException {
		class_def_items.clear();
		if (class_defs != null) {
			reader.seek(class_defs.offset);
			int items = (int) (class_defs.size);
			for (int i = 0; i < items; i++) {
				ClassDef def = new ClassDef();
				def.class_idx = reader.readUnsignedInt();
				def.access_flags = reader.readUnsignedInt();
				def.superclass_idx = reader.readUnsignedInt();
				def.interfaces_off = reader.readUnsignedInt();
				def.source_file_idx = reader.readUnsignedInt();
				def.annotations_off = reader.readUnsignedInt();
				def.class_data_off = reader.readUnsignedInt();
				def.static_values_off = reader.readUnsignedInt();
				class_def_items.put(def.class_idx, def);
			}
			
		}
	}

	private void parseLinkData() throws IOException {
		if (link == null) {
			link_data = new byte[0];
		} else {
			reader.seek(link.offset);
			link_data = new byte[(int) link.size];
			reader.read(link_data);
		}
	}

	private void parseData() throws IOException {
		if (data == null) {
			data_seg = new byte[0];
		} else {
			reader.seek(data.offset);
			data_seg = new byte[(int) data.size];
			reader.read(data_seg);
		}
	}

	private void parseStringTable() throws IOException, DexFormatException {
		string_table.clear();
		for (long offset : string_id_items) {
			if (offset < 0 || offset >= file_size) {
				throw new DexFormatException("Invalid string table offset");
			}
			reader.seek(offset);
			string_table.add(reader.readUTF());
		}
	}
	
	private void parsePrototypeTables() throws IOException, DexFormatException {
		for (ProtoId proto : proto_id_items) {
			proto.parameters.clear();
			if (proto.parameters_off != 0) {
				reader.seek(proto.parameters_off);
				int len = (int) reader.readUnsignedInt();
				for (int j = 0; j < len; j++) {
					proto.parameters.add(reader.readUnsignedShort());
				}
			}
		}
	}
	
	private void parseClassTables() throws IOException, DexFormatException {
		for (ClassDef klass : class_def_items.values()) {
			klass.interfaces = new HashSet<Integer>();
			if (klass.interfaces_off != 0) {
				long offset = reader.getFilePointer();
				reader.seek(klass.interfaces_off);
				int len = (int) reader.readUnsignedInt();
				for (int j = 0; j < len; j++) {
					klass.interfaces.add(reader.readUnsignedShort());
				}
				reader.seek(offset);
			}
			klass.static_values = new ArrayList<Value>();
			if (klass.static_values_off != 0) {
				long offset = reader.getFilePointer();
				reader.seek(klass.static_values_off);
				int len = (int) reader.readUnsignedLEB128();
				for (int j = 0; j < len; j++) {
					klass.static_values.add(reader.readEncodedValue());
				}
				reader.seek(offset);
			}
			klass.class_annotations = new LinkedHashMap<Long, Map<Long, Value>>();
			klass.field_annotations = new LinkedHashMap<Long, Map<Long, Map<Long, Value>>>();
			klass.method_annotations = new LinkedHashMap<Long, Map<Long, Map<Long, Value>>>();
			klass.parameter_annotations = new LinkedHashMap<Long, List<Map<Long, Map<Long,Value>>>>();
			if (klass.annotations_off != 0) {
				long offset = reader.getFilePointer();
				reader.seek(klass.annotations_off);
				long class_annotations_off = reader.readUnsignedInt();
				long fields_size = reader.readUnsignedInt();
				long annotated_methods_size = reader.readUnsignedInt();
				long annotated_parameters_size = reader.readUnsignedInt();
				for (int j = 0; j < (int) fields_size; j++) {
					long field_idx = reader.readUnsignedInt();
					long annotations_off = reader.readUnsignedInt();
					long offset2 = reader.getFilePointer();
					reader.seek(annotations_off);
					Map<Long, Map<Long, Value>> annotations = new LinkedHashMap<Long, Map<Long,Value>>();
					long size = reader.readUnsignedInt();
					for (int k = 0; k < (int) size; k++) {
						long annotation_off = reader.readUnsignedInt();
						long offset3 = reader.getFilePointer();
						reader.seek(annotation_off);
						int visibility = reader.readUnsignedByte();
						Map.Entry<Long, Map<Long, Value>> annotation = reader.readEncodedAnnotation();
						annotations.put(annotation.getKey(), annotation.getValue());
						reader.seek(offset3);
					}
					klass.field_annotations.put(field_idx, annotations);
					reader.seek(offset2);
				}
				for (int j = 0; j < (int) annotated_methods_size; j++) {
					long method_idx = reader.readUnsignedInt();
					long annotations_off = reader.readUnsignedInt();
					long offset2 = reader.getFilePointer();
					reader.seek(annotations_off);
					Map<Long, Map<Long, Value>> annotations = new LinkedHashMap<Long, Map<Long,Value>>();
					long size = reader.readUnsignedInt();
					for (int k = 0; k < (int) size; k++) {
						long annotation_off = reader.readUnsignedInt();
						long offset3 = reader.getFilePointer();
						reader.seek(annotation_off);
						int visibility = reader.readUnsignedByte();
						Map.Entry<Long, Map<Long, Value>> annotation = reader.readEncodedAnnotation();
						annotations.put(annotation.getKey(), annotation.getValue());
						reader.seek(offset3);
					}
					klass.method_annotations.put(method_idx, annotations);
					reader.seek(offset2);
				}
				for (int j = 0; j < (int) annotated_parameters_size; j++) {
					long method_idx = reader.readUnsignedInt();
					long args_off = reader.readUnsignedInt();
					long offset2 = reader.getFilePointer();
					reader.seek(args_off);
					long args_size = reader.readUnsignedInt();
					List<Map<Long, Map<Long, Value>>> args = new ArrayList<Map<Long, Map<Long, Value>>>((int) args_size);
					for (int k = 0; k < (int) args_size; k++) {
						long annotations_off = reader.readUnsignedInt();
						long offset3 = reader.getFilePointer();
						reader.seek(annotations_off);
						Map<Long, Map<Long, Value>> annotations = new LinkedHashMap<Long, Map<Long, Value>>();
						long size = reader.readUnsignedInt();
						for (int l = 0; l < (int) size; l++) {
							long annotation_off = reader.readUnsignedInt();
							long offset4 = reader.getFilePointer();
							reader.seek(annotation_off);
							int visibility = reader.readUnsignedByte();
							Map.Entry<Long, Map<Long, Value>> annotation = reader.readEncodedAnnotation();
							annotations.put(annotation.getKey(), annotation.getValue());
							reader.seek(offset4);
						}
						args.add(annotations);
						reader.seek(offset3);
					}
					klass.parameter_annotations.put(method_idx, args);
					reader.seek(offset2);
				}
				if (class_annotations_off != 0) {
					reader.seek(class_annotations_off);
					long size = reader.readUnsignedInt();
					for (int k = 0; k < (int) size; k++) {
						long annotation_off = reader.readUnsignedInt();
						long offset2 = reader.getFilePointer();
						reader.seek(annotation_off);
						int visibility = reader.readUnsignedByte();
						Map.Entry<Long, Map<Long, Value>> annotation = reader.readEncodedAnnotation();
						klass.class_annotations.put(annotation.getKey(), annotation.getValue());
						reader.seek(offset2);
					}
				}
				reader.seek(offset);
			}
		}
	}
	
	private void parseClassData() throws IOException {
		for (ClassDef klass : class_def_items.values()) {
			if (klass.class_data_off != 0) {
				reader.seek(klass.class_data_off);
				long static_fields_size = reader.readUnsignedLEB128();
				long instance_fields_size = reader.readUnsignedLEB128();
				long direct_methods_size = reader.readUnsignedLEB128();
				long virtual_methods_size = reader.readUnsignedLEB128();
				klass.static_fields = new LinkedHashMap<Long, EnumSet<Access>>((int) static_fields_size);
				klass.instance_fields = new LinkedHashMap<Long, EnumSet<Access>>((int) instance_fields_size);
				klass.direct_methods = new LinkedHashMap<Long, MethodDef>((int) direct_methods_size);
				klass.virtual_methods = new LinkedHashMap<Long, MethodDef>((int) virtual_methods_size);
				long field_index = 0;
				for (int j = 0; j < (int) static_fields_size; j++) {
					long field_idx_diff = reader.readUnsignedLEB128();
					field_index += field_idx_diff;
					long access_flags = reader.readUnsignedLEB128();
					klass.static_fields.put(field_index, Access.fromFlags(access_flags));
				}
				field_index = 0;
				for (int j = 0; j < (int) instance_fields_size; j++) {
					long field_idx_diff = reader.readUnsignedLEB128();
					field_index += field_idx_diff;
					long access_flags = reader.readUnsignedLEB128();
					klass.instance_fields.put(field_index, Access.fromFlags(access_flags));
				}
				long method_index = 0;
				for (int j = 0; j < (int) direct_methods_size; j++) {
					long method_idx_diff = reader.readUnsignedLEB128();
					method_index += method_idx_diff;
					long access_flags = reader.readUnsignedLEB128();
					long code_off = reader.readUnsignedLEB128();
					MethodDef method = new MethodDef(method_index, Access.fromFlags(access_flags), code_off);
					klass.direct_methods.put(method_index, method);
				}
				method_index = 0;
				for (int j = 0; j < (int) virtual_methods_size; j++) {
					long method_idx_diff = reader.readUnsignedLEB128();
					method_index += method_idx_diff;
					long access_flags = reader.readUnsignedLEB128();
					long code_off = reader.readUnsignedLEB128();
					MethodDef method = new MethodDef(method_index, Access.fromFlags(access_flags), code_off);
					klass.virtual_methods.put(method_index, method);
				}
			} else {
				klass.static_fields = new LinkedHashMap<Long, EnumSet<Access>>();
				klass.instance_fields = new LinkedHashMap<Long, EnumSet<Access>>();
				klass.direct_methods = new LinkedHashMap<Long, MethodDef>();
				klass.virtual_methods = new LinkedHashMap<Long, MethodDef>();
			}
		}
	}
	
	private void parseCode() throws IOException, DexFormatException {
		for (ClassDef klass : class_def_items.values()) {
			for (Map.Entry<Long, MethodDef> entry : klass.direct_methods.entrySet()) {
				MethodDef method = entry.getValue();
				if (method.code_off != 0) {
					reader.seek(method.code_off);
					method.registers_size = reader.readUnsignedShort();
					method.ins_size = reader.readUnsignedShort();
					method.outs_size = reader.readUnsignedShort();
					int tries_size = reader.readUnsignedShort();
					method.debug_info_off = reader.readUnsignedInt();
					long insns_size = reader.readUnsignedInt();
					short[] instructions = new short[(int) insns_size];
					for (int j = 0; j < (int) insns_size; j++) {
						instructions[j] = (short) reader.readUnsignedShort();
					}
					try {
						method.instructions = Instructions.parse(instructions);
					} catch (IllegalInstructionException e) {
						throw new DexFormatException("Invalid instruction", e);
					}
					method.exceptions = new HashSet<TryDef>();
					if (tries_size > 0) {
						if ((insns_size & 1) == 1) {
							reader.readUnsignedShort();
						}
						for (int j = 0; j < (int) tries_size; j++) {
							TryDef exception = new TryDef(reader.readUnsignedInt(), reader.readUnsignedShort(), reader.readUnsignedShort());
							method.exceptions.add(exception);
						}
						long handlers_pos = reader.getFilePointer();
						long handlers_size = reader.readUnsignedLEB128();
						for (TryDef exception : method.exceptions) {
							reader.seek(handlers_pos + exception.handler_off);
							int size = reader.readLEB128();
							boolean catch_all;
							int total_handlers;
							if (size > 0) {
								catch_all = false;
								total_handlers = size;
							} else {
								catch_all = true;
								total_handlers = -size;
							}
							exception.catches = new LinkedHashMap<Long, Long>(total_handlers);
							for (int k = 0; k < total_handlers; k++) {
								long type_idx = reader.readUnsignedLEB128();
								long addr = reader.readUnsignedLEB128();
								exception.catches.put(type_idx, addr);
							}
							if (catch_all) {
								exception.catch_all_addr = reader.readUnsignedLEB128();
							} else {
								exception.catch_all_addr = -1;
							}
						}
					}
				}
				if (method.debug_info_off != 0) {
					reader.seek(method.debug_info_off);
					// TODO parse debug info
				}
			}
			for (Long method_idx : klass.virtual_methods.keySet()) {
				MethodDef method = klass.virtual_methods.get(method_idx);
				if (method.code_off != 0) {
					reader.seek(method.code_off);
					method.registers_size = reader.readUnsignedShort();
					method.ins_size = reader.readUnsignedShort();
					method.outs_size = reader.readUnsignedShort();
					int tries_size = reader.readUnsignedShort();
					method.debug_info_off = reader.readUnsignedInt();
					long insns_size = reader.readUnsignedInt();
					short[] instructions = new short[(int) insns_size];
					for (int j = 0; j < (int) insns_size; j++) {
						instructions[j] = (short) reader.readUnsignedShort();
					}
					try {
						method.instructions = Instructions.parse(instructions);
					} catch (IllegalInstructionException e) {
						throw new DexFormatException("Invalid program", e);
					}
					method.exceptions = new HashSet<TryDef>();
					if (tries_size > 0) {
						if ((insns_size & 1) == 1) {
							reader.readUnsignedShort();
						}
						for (int j = 0; j < (int) tries_size; j++) {
							TryDef exception = new TryDef(reader.readUnsignedInt(), reader.readUnsignedShort(), reader.readUnsignedShort());
							method.exceptions.add(exception);
						}
						long handlers_pos = reader.getFilePointer();
						long handlers_size = reader.readUnsignedLEB128();
						for (TryDef exception : method.exceptions) {
							reader.seek(handlers_pos + exception.handler_off);
							int size = reader.readLEB128();
							boolean catch_all;
							int total_handlers;
							if (size > 0) {
								catch_all = false;
								total_handlers = size;
							} else {
								catch_all = true;
								total_handlers = -size;
							}
							exception.catches = new LinkedHashMap<Long, Long>(total_handlers);
							for (int k = 0; k < total_handlers; k++) {
								long type_idx = reader.readUnsignedLEB128();
								long addr = reader.readUnsignedLEB128();
								exception.catches.put(type_idx, addr);
							}
							if (catch_all) {
								exception.catch_all_addr = reader.readUnsignedLEB128();
							} else {
								exception.catch_all_addr = -1;
							}
						}
					}
				}
				if (method.debug_info_off != 0) {
					reader.seek(method.debug_info_off);
					// TODO parse debug info
				}
			}
		}
	}

	private void extendStaticFields() {
		// compile the list of types and assign default values
		Map<Integer, Value> typemap = new HashMap<Integer, Value>();
		for (int i = 0; i < type_id_items.size(); i++) {
			String typename = lookupString(type_id_items.get(i).intValue());
			Value defvalue;
			if (typename.length() > 0) {
				defvalue = new Value(Type.fromCode(typename.charAt(0)));
			} else {
				defvalue = new Value(Type.VALUE_UNKNOWN);
			}
			typemap.put(i, defvalue);
		}
		// extend the static value list
		for (ClassDef klass : class_def_items.values()) {
			List<Map.Entry<Long, EnumSet<Access>>> entries = new ArrayList<Map.Entry<Long, EnumSet<Access>>>(klass.static_fields.entrySet());
			// zero-extend the list of static field values
			for (int i = klass.static_values.size(); i < entries.size(); i++) {
				klass.static_values.add(typemap.get(field_id_items.get(entries.get(i).getKey().intValue()).type_idx));
			}
		}
	}

	/**
	 * Returns the code for a method, or null if no method definition is available.
	 * @param methodid a method id
	 * @return a map of address-instruction pairs, ordered by address or null
	 */
	public SortedMap<Integer, Instruction> getCode(int methodid) {
		if (methodid < 0 || methodid >= method_id_items.size()) return null;
		MethodId method = method_id_items.get(methodid);
		if (class_def_items.containsKey(new Long(method.class_idx))) {
			ClassDef klass = class_def_items.get(new Long(method.class_idx));
			if (klass.direct_methods.containsKey(new Long(methodid))) {
				return klass.direct_methods.get(new Long(methodid)).instructions;
			}
			if (klass.virtual_methods.containsKey(new Long(methodid))) {
				return klass.virtual_methods.get(new Long(methodid)).instructions;
			}
		}
		return null;
	}
	
	@Override
	public int numberOfStrings() {
		return string_table.size();
	}
	
	@Override
	public String lookupString(int index) {
		if (index < 0 || index >= string_table.size()) return null;
		return string_table.get(index);
	}

	@Override
	public int numberOfMethods() {
		return method_id_items.size();
	}
	
	@Override
	public MethodDescriptor lookupMethod(int index) {
		if (index < 0 || index >= method_id_items.size()) return null;
		MethodId method = method_id_items.get(index);
		ProtoId proto = proto_id_items.get(method.proto_idx);
		List<Integer> params = new ArrayList<Integer>(proto.parameters);
		return new MethodDescriptor(index, method.class_idx, lookupString((int) method.name_idx), (int) proto.return_type_idx, params);
	}

	@Override
	public ClassDescriptor lookupClass(int index) {
		if (!class_def_items.containsKey(new Long(index))) return null;
		ClassDef klass = class_def_items.get(new Long(index));
		List<Value> statics = new ArrayList<Value>(klass.static_values);
		List<ClassFieldDescriptor> fields = new ArrayList<ClassFieldDescriptor>();
		List<Map.Entry<Long, EnumSet<Access>>> entries = new ArrayList<Map.Entry<Long,EnumSet<Access>>>(klass.static_fields.entrySet());
		if (entries.size() != statics.size()) {
			throw new DexValidationException("Number of static default values does not match number of static fields");
		}
		for (int i = 0; i < statics.size(); i++) {
			Map.Entry<Long, EnumSet<Access>> entry = entries.get(i);
			Value value = statics.get(i);
			FieldDescriptor field = lookupField(entry.getKey().intValue());
			if (klass.class_idx != field.classid) {
				throw new DexValidationException("Field descriptor belongs to different class");
			}
			if (!entry.getValue().contains(Access.ACC_STATIC)) {
				throw new DexValidationException("Static field lacks static modifier");
			}
			fields.add(new StaticClassFieldDescriptor(field.fieldid, field.classid, field.typeid, field.name, Access.toJavaModifier(entry.getValue()), value));
		}
		for (Map.Entry<Long, EnumSet<Access>> entry : klass.instance_fields.entrySet()) {
			FieldDescriptor field = lookupField(entry.getKey().intValue());
			if (klass.class_idx != field.classid) {
				throw new DexValidationException("Field descriptor belongs to different class");
			}
			if (entry.getValue().contains(Access.ACC_STATIC)) {
				throw new DexValidationException("Instance field has static modifier");
			}
			fields.add(new ClassFieldDescriptor(field.fieldid, field.classid, field.typeid, field.name, Access.toJavaModifier(entry.getValue())));
		}
		List<ClassMethodDescriptor> methods = new ArrayList<ClassMethodDescriptor>();
		for (Map.Entry<Long, MethodDef> entry : klass.direct_methods.entrySet()) {
			MethodDescriptor method = lookupMethod(entry.getKey().intValue());
			MethodDef def = entry.getValue();
			if (klass.class_idx != method.classid) {
				throw new DexValidationException("Method descriptor belongs to different class");
			}
			if (entry.getKey().intValue() != def.method_idx) {
				throw new DexValidationException("Method definition has wrong method identifier");
			}
			Set<TryDescriptor> exs = new HashSet<TryDescriptor>();
			for (TryDef tr : def.exceptions) {
				Map<Integer, Integer> catches = new LinkedHashMap<Integer, Integer>();
				for (Map.Entry<Long, Long> entry2 : tr.catches.entrySet()) {
					catches.put(entry2.getKey().intValue(), entry2.getValue().intValue());
				}
				exs.add(new TryDescriptor((int) tr.start_addr, (int) tr.insn_count, catches, (int) tr.catch_all_addr));
			}
			methods.add(new ClassMethodDescriptor(method.methodid, method.classid, method.name, Access.toJavaModifier(def.access_flags), method.returntype, method.parameters, def.registers_size, def.ins_size, def.outs_size, exs));
		}
		for (Map.Entry<Long, MethodDef> entry : klass.virtual_methods.entrySet()) {
			MethodDescriptor method = lookupMethod(entry.getKey().intValue());
			MethodDef def = entry.getValue();
			if (klass.class_idx != method.classid) {
				throw new DexValidationException("Method descriptor belongs to different class");
			}
			if (entry.getKey().intValue() != def.method_idx) {
				throw new DexValidationException("Method definition has wrong method identifier");
			}
			Set<TryDescriptor> exs = new HashSet<TryDescriptor>();
			for (TryDef tr : def.exceptions) {
				Map<Integer, Integer> catches = new LinkedHashMap<Integer, Integer>();
				for (Map.Entry<Long, Long> entry2 : tr.catches.entrySet()) {
					catches.put(entry2.getKey().intValue(), entry2.getValue().intValue());
				}
				exs.add(new TryDescriptor((int) tr.start_addr, (int) tr.insn_count, catches, (int) tr.catch_all_addr));
			}
			methods.add(new ClassMethodDescriptor(method.methodid, method.classid, method.name, Access.toJavaModifier(def.access_flags), method.returntype, method.parameters, def.registers_size, def.ins_size, def.outs_size, exs));
		}
		return new ClassDescriptor((int) klass.class_idx, Access.toJavaModifier(Access.fromFlags(klass.access_flags)), (int) klass.superclass_idx, klass.interfaces, fields, methods);
	}

	@Override
	public int numberOfFields() {
		return field_id_items.size();
	}
	
	@Override
	public FieldDescriptor lookupField(int index) {
		if (index < 0 || index >= field_id_items.size()) return null;
		FieldId field = field_id_items.get(index);
		return new FieldDescriptor(index, field.class_idx, field.type_idx, lookupString((int) field.name_idx));
	}

	@Override
	public int numberOfTypes() {
		return type_id_items.size();
	}
	
	@Override
	public String lookupType(int index) {
		if (index < 0 || index >= type_id_items.size()) return null;
		return string_table.get(type_id_items.get(index).intValue());
	}
	
}
