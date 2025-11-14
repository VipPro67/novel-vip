package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Report.ReportDTO;
import com.novel.vippro.Models.Report;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = { UserMapper.class, NovelMapper.class, ChapterMapper.class, CommentMapper.class })
public interface ReportMapper {

    ReportDTO ReporttoDTO(Report report);

    Report DTOtoReport(ReportDTO reportDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateReportFromDTO(ReportDTO dto, @MappingTarget Report report);
}
