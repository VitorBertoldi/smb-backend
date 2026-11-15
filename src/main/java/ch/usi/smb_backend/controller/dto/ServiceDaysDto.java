package ch.usi.smb_backend.controller.dto;

import java.time.LocalDate;
import java.util.List;

public record ServiceDaysDto(List<LocalDate> dates) {
}
