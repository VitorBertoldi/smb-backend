package ch.usi.smb_backend.repository.entity;

import java.util.List;
import org.springframework.data.repository.Repository;

public interface PathSegmentEntityRepository extends Repository<PathSegmentEntity, Integer> {

	List<PathSegmentEntity> findAll();
}
