package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Report.ReportDTO;
import com.novel.vippro.Models.Report;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReportMapper {
	@Autowired
	private ModelMapper modelMapper;

	public ReportDTO ReporttoDTO(Report report) {
		return modelMapper.map(report, ReportDTO.class);
	}

	public Report DTOtoReport(ReportDTO reportDTO) {
		return modelMapper.map(reportDTO, Report.class);
	}

	public void updateReportFromDTO(ReportDTO dto, Report report) {
		modelMapper.map(dto, report);
	}
}
