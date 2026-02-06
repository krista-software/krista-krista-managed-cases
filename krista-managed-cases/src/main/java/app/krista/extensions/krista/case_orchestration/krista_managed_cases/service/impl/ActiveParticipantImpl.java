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

package app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.impl;

import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.Account;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.ActiveParticipant;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.Queue;

public class ActiveParticipantImpl implements ActiveParticipant {

    private final Queue queue;
    private final Account account;

    public ActiveParticipantImpl(Queue queue, Account account) {
        this.queue = queue;
        this.account = account;
    }

    @Override
    public Queue getQueue() {
        return queue;
    }

    @Override
    public Account getParticipant() {
        return account;
    }

}
