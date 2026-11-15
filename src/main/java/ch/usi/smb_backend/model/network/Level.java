package ch.usi.smb_backend.model.network;

/**
 * Whether something is underground. Three values instead of a nullable flag, so "we don't know"
 * can't be read as "no".
 */
public enum Level {
	UNDERGROUND,
	SURFACE,
	UNKNOWN
}
