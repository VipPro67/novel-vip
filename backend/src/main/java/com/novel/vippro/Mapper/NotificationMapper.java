package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Notification.NotificationDTO;
import com.novel.vippro.Models.Notification;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "userId", source = "user.id")
    NotificationDTO NotificationtoDTO(Notification notification);

    Notification DTOtoNotification(NotificationDTO notificationDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateNotificationFromDTO(NotificationDTO dto, @MappingTarget Notification notification);
}
