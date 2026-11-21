package ch.usi.smb_backend.model.network;

/**
 * What a path segment runs on; mirrors {@code path_segments.network}. The gold data on Azure
 * (2026-11) uses RAIL, METRO, TRAM, ROAD and WATER; TRACK and ROPE are kept from the first schema.
 */
public enum NetworkType {
	RAIL,
	METRO,
	TRAM,
	TRACK,
	ROAD,
	WATER,
	ROPE
}
