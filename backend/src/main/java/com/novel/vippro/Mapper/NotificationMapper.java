package com.novel.vippro.Mapper;

import com.novel.vippro.DTO.Notification.NotificationDTO;
import com.novel.vippro.Models.Notification;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
	@Autowired
	private ModelMapper modelMapper;

	public NotificationDTO NotificationtoDTO(Notification notification) {
		return modelMapper.map(notification, NotificationDTO.class);
	}

	public void updateNotificationFromDTO(NotificationDTO dto, Notification notification) {
		modelMapper.map(dto, notification);
	}

	public Notification DTOtoNotification(NotificationDTO notificationDTO) {
		return modelMapper.map(notificationDTO, Notification.class);
	}
}
