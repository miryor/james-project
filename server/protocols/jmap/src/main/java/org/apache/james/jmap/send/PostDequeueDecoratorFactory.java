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

package org.apache.james.jmap.send;

import javax.inject.Inject;

import org.apache.james.mailbox.MailboxManager;
import org.apache.james.mailbox.store.mail.MailboxMapperFactory;
import org.apache.james.mailbox.store.mail.MessageMapperFactory;
import org.apache.james.queue.api.MailQueue.MailQueueItem;
import org.apache.james.queue.api.MailQueueItemDecoratorFactory;

public class PostDequeueDecoratorFactory implements MailQueueItemDecoratorFactory {
    private final MailboxManager mailboxManager;
    private final MessageMapperFactory messageMapperFactory;
    private final MailboxMapperFactory mailboxMapperFactory;

    @Inject
    public PostDequeueDecoratorFactory(MailboxManager mailboxManager,
            MessageMapperFactory messageMapperFactory,
            MailboxMapperFactory mailboxMapperFactory) {
                this.mailboxManager = mailboxManager;
                this.messageMapperFactory = messageMapperFactory;
                this.mailboxMapperFactory = mailboxMapperFactory;
    }

    @Override
    public MailQueueItemDecorator decorate(MailQueueItem mailQueueItem) {
        return new PostDequeueDecorator(mailQueueItem, mailboxManager, messageMapperFactory, mailboxMapperFactory);
    }

}
