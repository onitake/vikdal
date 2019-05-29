package ch.seto.vikdal;

/**
 * A listener interface for progress updates.
 * Progress is reported in incremental, arbitrarily small steps from 0.0f to 1.0f.
 */
public interface ProgressListener {
	/**
	 * Called when part of an action has been completed.
	 * @param progress a value between 0.0f and 1.0f (inclusive)
	 */
	public void progressUpdated(float progress);
}
