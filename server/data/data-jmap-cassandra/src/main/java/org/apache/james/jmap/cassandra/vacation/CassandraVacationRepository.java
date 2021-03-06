/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.jmap.cassandra.vacation;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.apache.james.jmap.api.vacation.AccountId;
import org.apache.james.jmap.api.vacation.Vacation;
import org.apache.james.jmap.api.vacation.VacationRepository;

public class CassandraVacationRepository implements VacationRepository {

    private final CassandraVacationDAO cassandraVacationDAO;

    @Inject
    public CassandraVacationRepository(CassandraVacationDAO cassandraVacationDAO) {
        this.cassandraVacationDAO = cassandraVacationDAO;
    }

    @Override
    public CompletableFuture<Void> modifyVacation(AccountId accountId, Vacation vacation) {
        return cassandraVacationDAO.modifyVacation(accountId, vacation);
    }

    @Override
    public CompletableFuture<Vacation> retrieveVacation(AccountId accountId) {
        return cassandraVacationDAO.retrieveVacation(accountId).thenApply(optional -> optional.orElse(DEFAULT_VACATION));
    }
}
