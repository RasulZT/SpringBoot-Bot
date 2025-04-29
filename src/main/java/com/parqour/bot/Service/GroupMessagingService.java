package com.parqour.bot.Service;

import com.parqour.bot.Singleton.Ticket;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

public interface GroupMessagingService {
    SendMessage sendMessageToGroup(Ticket ticket, String groupChatId);

    StringBuilder createTicketInfoToParkingGroup(Ticket ticket, ResourceBundle bundle, String languageCode);

    StringBuilder createTicketInfoToServiceGroup(Ticket ticket);

    List<String> getDutyOperators();
}
