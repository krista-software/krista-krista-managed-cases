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

import java.util.*;
import java.util.function.Predicate;
import javax.inject.Inject;
import app.krista.extension.authorization.AuthorizationException;
import app.krista.ksdk.accounts.Account;
import app.krista.ksdk.accounts.AccountProvider;
import org.jvnet.hk2.annotations.Service;

@Service
public class CachedAccountProvider implements AccountProvider {

    private final ThreadLocal<Map<String, Account>> accounts = ThreadLocal.withInitial(HashMap::new);
    private final AccountProvider accountProvider;

    @Inject
    public CachedAccountProvider(AccountProvider accountProvider) {
        this.accountProvider = accountProvider;
    }

    @Override
    public Account getAccount(String accountId) throws AuthorizationException, NoSuchElementException {
        Map<String, Account> stringAccountMap = accounts.get();
        if (!stringAccountMap.containsKey(accountId)) {
            stringAccountMap.put(accountId, accountProvider.getAccount(accountId));
        }
        return stringAccountMap.get(accountId);
    }

    @Override
    public synchronized Account lookupAccount(String primaryEmail) throws AuthorizationException {
        Map<String, Account> stringAccountMap = accounts.get();
        if (!stringAccountMap.containsKey(primaryEmail)) {
            stringAccountMap.put(primaryEmail, accountProvider.lookupAccount(primaryEmail));
        }
        return stringAccountMap.get(primaryEmail);
    }

    @Override
    public Iterable<? extends Account> lookupAccounts(String s, Predicate<Account> predicate)
            throws AuthorizationException, IllegalArgumentException {
        return accountProvider.lookupAccounts(s, predicate);
    }

    @Override
    public Map<String, Set<String>> getAccountHierarchy(List<String> emailList) throws AuthorizationException {
        return accountProvider.getAccountHierarchy(emailList);
    }

    public void clear() {
        accounts.get().clear();
    }

}
