
package org.apache.activemq.transport.failover;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.Socket;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.Command;
import org.apache.activemq.command.LocalTransactionId;
import org.apache.activemq.command.MessageAck;
import org.apache.activemq.command.TransactionInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FailoverTimeoutTest {
	public void doTestInterleaveAndException(final ActiveMQConnection connection, final Command command) throws Exception {

		connection.start();

		connection.setExceptionListener(new ExceptionListener() {
			@Override
			public void onException(JMSException exception) {
				try {
					LOG.info("Deal with exception - invoke op that may block pending outstanding oneway");
					// try and invoke on connection as part of handling exception
					connection.asyncSendPacket(command);
				} catch (Exception e) {
				}
			}
		});

		final ExecutorService executorService = Executors.newCachedThreadPool();

		final int NUM_TASKS = 200;
		final CountDownLatch enqueueOnExecutorDone = new CountDownLatch(NUM_TASKS);

// let a few tasks delay a bit
		final AtomicLong sleepMillis = new AtomicLong(1000);
		for (int i = 0; i < NUM_TASKS; i++) {
			executorService.submit(new Runnable() {
				@Override
				public void run() {
					try {
						TimeUnit.MILLISECONDS.sleep(Math.max(0, sleepMillis.addAndGet(-50)));
						connection.asyncSendPacket(command);
					} catch (Exception e) {
					} finally {
						enqueueOnExecutorDone.countDown();
					}
				}
			});
		}

		while (enqueueOnExecutorDone.getCount() > (NUM_TASKS - 10)) {
			enqueueOnExecutorDone.await(20, TimeUnit.MILLISECONDS);
		}

// force IOException
		final Socket socket = connection.getTransport().narrow(Socket.class);
		socket.close();

		executorService.shutdown();

		assertTrue("all ops finish", enqueueOnExecutorDone.await(15, TimeUnit.SECONDS));
	}
}