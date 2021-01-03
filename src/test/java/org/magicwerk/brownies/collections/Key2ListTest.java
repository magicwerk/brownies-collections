/*
 * Copyright 2013 by Thomas Mauch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id$
 */
package org.magicwerk.brownies.collections;

import java.util.List;

import org.magictest.client.Assert;
import org.magictest.client.Capture;
import org.magictest.client.Trace;
import org.magicwerk.brownies.collections.Key1ListTest.TicketList;
import org.magicwerk.brownies.collections.TestHelper.Ticket;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.slf4j.Logger;

/**
 * Test of Key2List.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class Key2ListTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

    public static void main(String[] args) {
        test();
    }

    static void test() {
    	//testInvalidate();
    	testKey2List();
    }

    static class TicketList extends Key2List<Ticket,Integer,String> {
    	public TicketList() {
    		getBuilder().withPrimaryKey1Map(Ticket.IdMapper).withUniqueKey2Map(Ticket.ExtIdMapper).build();
    	}
    }

    @Trace(traceClass="org.magicwerk.brownies.collections.Key2ListTest$TicketList", traceMethod="/.*/", parameters=Trace.ALL_PARAMS|Trace.THIS, result=Trace.THIS|Trace.RESULT)
    public static void testExtends() {
    	TicketList tc = new TicketList();
    	Ticket t1 = new Ticket(1, "extId1", "text1");
    	tc.add(t1);
    	TicketList tc1 = (TicketList) tc.clone();
    	TicketList tc2 = (TicketList) tc.copy();
    	TicketList tc3 = (TicketList) tc.crop();
    }

    @Trace(traceMethod="/.*/", parameters=Trace.ALL_PARAMS|Trace.THIS, result=Trace.THIS|Trace.RESULT)
    public static void testInvalidate() {
    	{
	        Key2List<Ticket,Integer,String> list = new Key2List.Builder<Ticket,Integer,String>().
	        		withKey1Map(Ticket.IdMapper).withKey1Duplicates(false).withKey1Sort(true).
	        		withKey2Map(Ticket.ExtIdMapper).withKey2Duplicates(true).withKey2Sort(true).build();
	        Ticket t1 = new Ticket(1, "extId1", "text1");
	        Ticket t2 = new Ticket(2, "extId2", "text2");
	        Ticket t3 = new Ticket(3, "extId3x", "text3");
	        Ticket t4 = new Ticket(4, "extId3x", "text4");
	        list.add(t1);
	        list.add(t3);
	        list.add(t2);
	        list.add(t4);

	        t1.id = 11;
	        list.getByKey1(11);
	        list.invalidate(t1);
	        list.getByKey1(11);

	        t2.id = 22;
	        list.getByKey1(22);
	        list.invalidateKey1(2, 22, null);
	        list.getByKey1(22);

	        t3.extId = "extId3a";
	        list.getByKey2("extId3a");
	        list.invalidateKey2("extId3x", "extId3a", t3);
	        list.getByKey2("extId3a");
    	}
    	{
            Key2List<Ticket,Integer,String> list = new Key2List.Builder<Ticket,Integer,String>().
            		withKey1Map(Ticket.IdMapper).withKey1Duplicates(false).withKey1Sort(true).withOrderByKey1(true).
            		withKey2Map(Ticket.ExtIdMapper).withKey2Duplicates(true).withKey2Sort(true).build();
            Ticket t = null;
            Ticket t1 = new Ticket(1, "extId1", "text1");
            Ticket t2 = new Ticket(2, "extId2", "text2");
            Ticket t3 = new Ticket(3, "extId3x", "text3");
            Ticket t4 = new Ticket(4, "extId3x", "text4");
            list.add(t1);
            list.add(t3);
            list.add(t2);
            list.add(t4);

            t1.id = 11;
            list.getByKey1(11);
            list.invalidate(t1);
            list.getByKey1(11);

            t2.id = 22;
            list.getByKey1(22);
            list.invalidateKey1(2, 22, null);
            t = list.getByKey1(22);
            Assert.assertTrue(t.getId() == 22);

            t3.extId = "extId3a";
            list.getByKey2("extId3a");
            list.invalidateKey2("extId3x", "extId3a", t3);
            list.getByKey2("extId3a");
        	}

    }

    @Trace(traceMethod="/.+/", parameters=Trace.THIS|Trace.ALL_PARAMS)
    public static void testKey2List() {

        Key2List<Ticket, Integer, String> tickets = new Key2List.Builder<Ticket, Integer, String>().
        		withNull(false).withConstraint(Ticket.Constraint).
        		withPrimaryKey1Map(Ticket.IdMapper).
        		withUniqueKey2Map(Ticket.ExtIdMapper).build();
        Ticket t1 = new Ticket(1, "ExtId1", "Ticket1");
        Ticket t2 = new Ticket(2, null,     "Ticket2");
        Ticket t3 = new Ticket(3, "ExtId3", "Ticket3");
        Ticket t4 = new Ticket(4, null,     "Ticket4");
        tickets.add(t1);
        tickets.add(t2);
        tickets.add(t3);
        tickets.add(t4);
    	LOG.info("Tickets: {}", tickets);

    	Ticket t = tickets.getByKey1(1);
    	LOG.info("Ticket: {}", t);
    	t = tickets.getByKey2("ExtId3");
    	LOG.info("Ticket: {}", t);

        // Exceptions
        tickets.add(new Ticket(1, "ExtId", "ERROR"));
        tickets.add(new Ticket(5, "ExtId3", "ERROR"));
        tickets.add(new Ticket(null, "ExtId", "ERROR"));
    	tickets.add(null);
        tickets.add(new Ticket(6, "EXT", "ERROR"));

    	tickets.removeByKey1(1);
    }

    static String format(List<Ticket> tickets) {
    	StringBuilder buf = new StringBuilder();
    	buf.append("[ ");
    	for (int i=0; i<tickets.size(); i++) {
    		if (i > 0) {
    			buf.append(", ");
    		}
    		buf.append(tickets.get(i).id);
    	}
    	buf.append(" ]");
    	return buf.toString();
    }

    @Capture
    public static void testCloneCopyCrop() {
        Key2List<Ticket, Integer, String> tickets1 = new Key2List.Builder<Ticket, Integer, String>().
        		withNull(false).withConstraint(Ticket.Constraint).
        		withPrimaryKey1Map(Ticket.IdMapper).
        		withUniqueKey2Map(Ticket.ExtIdMapper).build();
        doTestCloneCopyCrop(tickets1);

        Key2List<Ticket, Integer, String> tickets2 = new Key2List.Builder<Ticket, Integer, String>().
        		withNull(false).withConstraint(Ticket.Constraint).
        		withOrderByElem(true).
        		withPrimaryKey1Map(Ticket.IdMapper).
        		withUniqueKey2Map(Ticket.ExtIdMapper).build();
        doTestCloneCopyCrop(tickets2);
    }

    static void doTestCloneCopyCrop(Key2List<Ticket, Integer, String> tickets) {
        Ticket t1 = new Ticket(1, "ExtId1", "Ticket1");
        Ticket t2 = new Ticket(2, null,     "Ticket2");
        Ticket t3 = new Ticket(3, "ExtId3", "Ticket3");
        Ticket t4 = new Ticket(4, null,     "Ticket4");
        Ticket t5 = new Ticket(5, null,     "Ticket5");
        tickets.add(t1);
        tickets.add(t2);
        tickets.add(t3);
        tickets.add(t4);
        System.out.println("Orig: " + format(tickets));

        //-- Clone
        Key2List<Ticket, String, String> tickets2 = (Key2List<Ticket, String, String>) tickets.clone();
        System.out.println("-\nClone: " + format(tickets2));

        // Change original
        tickets.add(t5);
        System.out.println("Orig (changed): " + format(tickets));
        System.out.println("Clone (unchanged): " + format(tickets2));

        // Change clone
        tickets.remove(t5);
        tickets2.add(t5);
        System.out.println("Orig (unchanged): " + format(tickets));
        System.out.println("Clone (changed): " + format(tickets2));

//        System.out.println(getIdentityHashCode(tickets2));
//        System.out.println(getIdentityHashCode(getAnyBeanValues(tickets2, "tableColl")));
//        System.out.println(getIdentityHashCode(getAnyBeanValues(tickets2, "tableColl", "keyMaps[1]", "keysMap")));
//        System.out.println(getIdentityHashCode(getAnyBeanValues(tickets2, "tableColl", "keyMaps[2]", "keysMap")));
//        Assert.assertTrue(tickets2.equals(tickets));

        //-- Copy
        Key2List<Ticket, Integer, String> tickets3 = tickets.copy();
        System.out.println("-\nCopy: " + format(tickets3));
        Assert.assertTrue(tickets3.getClass() == tickets.getClass());
        Assert.assertTrue(tickets3.equals(tickets));

        // Change original
        tickets.add(t5);
        System.out.println("Orig (changed): " + format(tickets));
        System.out.println("Copy (unchanged): " + format(tickets3));

        // Change copy
        tickets.remove(t5);
        tickets3.add(t5);
        System.out.println("Orig (unchanged): " + format(tickets));
        System.out.println("Copy (changed): " + format(tickets3));

        //-- Crop
        Key2List<Ticket, Integer, String> tickets4 = tickets.crop();
        Assert.assertTrue(tickets4.getClass() == tickets.getClass());
        System.out.println("-\nCrop: " + format(tickets4));

        // Change original
        tickets.add(t5);
        System.out.println("Orig (changed): " + format(tickets));
        System.out.println("Crop (unchanged): " + format(tickets4));

        // Change crop
        tickets.remove(t5);
        tickets4.add(t5);
        System.out.println("Orig (unchanged): " + format(tickets));
        System.out.println("Crop (changed): " + format(tickets4));

    }

}


