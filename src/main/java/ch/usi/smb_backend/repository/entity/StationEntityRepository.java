package ch.usi.smb_backend.repository.entity;

import java.util.List;
import org.springframework.data.repository.Repository;

public interface StationEntityRepository extends Repository<StationEntity, String> {

	List<StationEntity> findAll();
}
