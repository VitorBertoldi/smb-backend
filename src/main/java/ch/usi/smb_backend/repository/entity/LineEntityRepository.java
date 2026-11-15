package ch.usi.smb_backend.repository.entity;

import java.util.List;
import org.springframework.data.repository.Repository;

public interface LineEntityRepository extends Repository<LineEntity, String> {

	List<LineEntity> findAll();
}
