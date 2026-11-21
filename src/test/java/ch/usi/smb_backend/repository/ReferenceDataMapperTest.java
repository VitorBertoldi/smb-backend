package ch.usi.smb_backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import ch.usi.smb_backend.model.geo.GeoPoint;
import ch.usi.smb_backend.model.network.Level;
import ch.usi.smb_backend.model.network.Line;
import ch.usi.smb_backend.model.network.NetworkType;
import ch.usi.smb_backend.model.network.Operator;
import ch.usi.smb_backend.model.network.PathSegment;
import ch.usi.smb_backend.model.network.Station;
import ch.usi.smb_backend.model.network.StationId;
import ch.usi.smb_backend.repository.entity.LineEntity;
import ch.usi.smb_backend.repository.entity.PathSegmentEntity;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

class ReferenceDataMapperTest {

	@Test
	void mapsACompleteLineAsItIs() {
		Line line = ReferenceDataMapper.toLine(
				lineRow("11:IC1", "IC1", "IC", "Schweizerische Bundesbahnen SBB", "11"));

		assertThat(line.name()).isEqualTo("IC1");
		assertThat(line.category()).isEqualTo("IC");
		assertThat(line.operator()).isEqualTo(new Operator("11", "Schweizerische Bundesbahnen SBB"));
	}

	@Test
	void fillsInMissingLineTextInsteadOfFailing() {
		Line line = ReferenceDataMapper.toLine(lineRow("85:XYZ", null, " ", null, null));

		assertThat(line.name()).isEqualTo("85:XYZ");
		assertThat(line.category()).isEqualTo("85:XYZ");
		assertThat(line.operator()).isEqualTo(new Operator("unknown", "unknown"));
	}

	@Test
	void namesTheOperatorByItsCodeWhenTheNameIsMissing() {
		Line line = ReferenceDataMapper.toLine(lineRow("11:IC1", "IC1", "IC", null, "11"));

		assertThat(line.operator()).isEqualTo(new Operator("11", "11"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"RAIL", "TRAM", "METRO", "TRACK"})
	void readsEveryRailNetworkAsTrack(String network) {
		Station zurich = station("8503000", 8.5402, 47.3782);
		Station bern = station("8507000", 7.4391, 46.9490);
		PathSegmentEntity row = BeanUtils.instantiateClass(PathSegmentEntity.class);
		ReflectionTestUtils.setField(row, "segmentId", 1);
		ReflectionTestUtils.setField(row, "fromStationId", "8503000");
		ReflectionTestUtils.setField(row, "toStationId", "8507000");
		ReflectionTestUtils.setField(row, "network", network);

		PathSegment segment = ReferenceDataMapper.toSegment(
				row, Map.of(zurich.id(), zurich, bern.id(), bern));

		assertThat(segment.network()).isEqualTo(NetworkType.TRACK);
	}

	private static Station station(String id, double longitude, double latitude) {
		return new Station(new StationId(id), "ch:1:sloid:" + id, id, new GeoPoint(longitude, latitude, 0), Level.UNKNOWN);
	}

	private static LineEntity lineRow(
			String lineId, String name, String category, String agencyName, String agencyCode) {
		LineEntity row = BeanUtils.instantiateClass(LineEntity.class);
		ReflectionTestUtils.setField(row, "lineId", lineId);
		ReflectionTestUtils.setField(row, "name", name);
		ReflectionTestUtils.setField(row, "category", category);
		ReflectionTestUtils.setField(row, "vehicleType", "TRAIN");
		ReflectionTestUtils.setField(row, "agencyName", agencyName);
		ReflectionTestUtils.setField(row, "agencyCode", agencyCode);
		return row;
	}
}
