/*
 * Krista Managed Cases Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>. 
 */

package app.krista.extensions.krista.case_orchestration.krista_managed_cases.service;

import java.io.IOException;
import java.util.List;
import app.krista.ksdk.inboxes.Inbox;

public interface Task {

    String getTaskId();

    String getMessage();

    Inbox getInbox();

    long getCreationTime();

    Account getCreator();

    void waitForParticipant(Queue queue) throws IOException;

    List<ActiveParticipant> getActiveParticipants();

    void join(app.krista.ksdk.accounts.Account account) throws IOException;

    void leave(app.krista.ksdk.accounts.Account account) throws IOException;

}
