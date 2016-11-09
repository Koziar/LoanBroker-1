/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loanbroker;

import config.RoutingKeys;
import entity.Bank;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 *
 * @author Mathias
 */
public class MessageProcessor {

    private static final String XML_ROOT_ELEMENT = "RecipientListRequest";
    private static final String XML_LOAN_ELEMENT = "LoanDetails";
    private static final String XML_SSN_ELEMENT = "ssn";
    private static final String XML_CREDIT_SCORE_ELEMENT = "creditScore";
    private static final String XML_LOAN_AMOUNT_ELEMENT = "loanAmount";
    private static final String XML_LOAN_DURATION_IN_MONTHS_ELEMENT = "loanDurationInMonths";
    private static final String XML_BANK_LIST_ELEMENT = "BankList";
    private static final String XML_BANK_ELEMENT = "bank";

    private XMLEvent endLine;
    private XMLEvent tab;

    private int creditScore;
    private ArrayList<Bank> availableBanks;
    private ArrayList<Bank> banks;
    private String response;
    private String message;
    private String ssn;
    private String loanAmount;
    private String loanDurationInMonths;

    public MessageProcessor(String message) {
        this.message = message;
        banks = new ArrayList<Bank>();
        availableBanks = new ArrayList<Bank>();
        availableBanks.add(new Bank(700, "cphbusiness.bankJSON", RoutingKeys.TranslatorOne));
        availableBanks.add(new Bank(400, "cphbusiness.bankXML", RoutingKeys.TranslatorTwo));
        availableBanks.add(new Bank(0, "firebug.BankXML", RoutingKeys.TranslatorThree));
    }

    public void processMessage() {
        try {
            parseXML();
            getBanks();
            writeMessage();
        }catch (Exception e) {
            response = e.getMessage();
        }
    }

    private void parseXML() throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader eventReader;
        try {
            eventReader = inputFactory.createXMLEventReader(new ByteArrayInputStream(message.getBytes()));

            boolean lookingForCreditScore = true;
            creditScore = -1;

            while (eventReader.hasNext() && lookingForCreditScore) {
                XMLEvent event = eventReader.nextEvent();

                if (event.isStartElement()) {
                    String partName = event.asStartElement().getName().getLocalPart();

                    if (partName.equals(XML_CREDIT_SCORE_ELEMENT)) {
                        creditScore = Integer.parseInt(eventReader.nextEvent().asCharacters().getData());
                        lookingForCreditScore = false;
                    } else if (partName.equals(XML_SSN_ELEMENT)) {
                        ssn = eventReader.nextEvent().asCharacters().getData();
                    } else if (partName.equals(XML_LOAN_AMOUNT_ELEMENT)) {
                        loanAmount = eventReader.nextEvent().asCharacters().getData();
                    } else if (partName.equals(XML_LOAN_DURATION_IN_MONTHS_ELEMENT)) {
                        loanDurationInMonths = eventReader.nextEvent().asCharacters().getData();
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new Exception(e.getMessage());
        }
    }

    private void getBanks() throws Exception {
        if (0 <= creditScore && creditScore <= 800) {
            banks = new ArrayList<Bank>();
            for (int i = 0; i < availableBanks.size(); i++) {
                if (availableBanks.get(i).getMinimumCreditScore() <= creditScore) {
                    banks.add(availableBanks.get(i));
                }
            }
        } else {
            throw new Exception("" + creditScore);
        }
    }

    private void writeMessage() throws Exception {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        XMLEventWriter eventWriter;
        try {
            eventWriter = outputFactory.createXMLEventWriter(outputStream);
            XMLEventFactory eventFactory = XMLEventFactory.newInstance();

            endLine = eventFactory.createDTD("\n");
            tab = eventFactory.createDTD("\t");

            eventWriter.add(eventFactory.createStartDocument());

            eventWriter.add(endLine);
            eventWriter.add(eventFactory.createStartElement("", "", XML_ROOT_ELEMENT));
            eventWriter.add(endLine);

            writeLoanToXML(eventWriter, eventFactory);

            eventWriter.add(tab);
            eventWriter.add(eventFactory.createStartElement("", "", XML_BANK_LIST_ELEMENT));
            eventWriter.add(endLine);

            for (Bank bank : banks) {
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(eventFactory.createStartElement("", "", XML_BANK_ELEMENT));
                eventWriter.add(eventFactory.createCharacters(bank.getName()));
                eventWriter.add(eventFactory.createEndElement("", "", XML_BANK_ELEMENT));
                eventWriter.add(endLine);
            }

            eventWriter.add(tab);
            eventWriter.add(eventFactory.createEndElement("", "", XML_BANK_LIST_ELEMENT));
            eventWriter.add(endLine);

            eventWriter.add(eventFactory.createEndElement("", "", XML_ROOT_ELEMENT));
            eventWriter.add(endLine);
            eventWriter.add(eventFactory.createEndDocument());
            eventWriter.close();

            response = outputStream.toString();
        } catch (XMLStreamException e) {
            throw new Exception(e.getMessage());
        }

    }

    private void writeLoanToXML(XMLEventWriter eventWriter, XMLEventFactory eventFactory) throws XMLStreamException {
        eventWriter.add(tab);
        eventWriter.add(eventFactory.createStartElement("", "", XML_LOAN_ELEMENT));
        eventWriter.add(endLine);

        eventWriter.add(tab);
        eventWriter.add(tab);
        eventWriter.add(eventFactory.createStartElement("", "", XML_SSN_ELEMENT));
        eventWriter.add(eventFactory.createCharacters(ssn));
        eventWriter.add(eventFactory.createEndElement("", "", XML_SSN_ELEMENT));
        eventWriter.add(endLine);

        eventWriter.add(tab);
        eventWriter.add(tab);
        eventWriter.add(eventFactory.createStartElement("", "", XML_CREDIT_SCORE_ELEMENT));
        eventWriter.add(eventFactory.createCharacters("" + creditScore));
        eventWriter.add(eventFactory.createEndElement("", "", XML_CREDIT_SCORE_ELEMENT));
        eventWriter.add(endLine);

        eventWriter.add(tab);
        eventWriter.add(tab);
        eventWriter.add(eventFactory.createStartElement("", "", XML_LOAN_AMOUNT_ELEMENT));
        eventWriter.add(eventFactory.createCharacters(loanAmount));
        eventWriter.add(eventFactory.createEndElement("", "", XML_LOAN_AMOUNT_ELEMENT));
        eventWriter.add(endLine);

        eventWriter.add(tab);
        eventWriter.add(tab);
        eventWriter.add(eventFactory.createStartElement("", "", XML_LOAN_DURATION_IN_MONTHS_ELEMENT));
        eventWriter.add(eventFactory.createCharacters(loanDurationInMonths));
        eventWriter.add(eventFactory.createEndElement("", "", XML_LOAN_DURATION_IN_MONTHS_ELEMENT));
        eventWriter.add(endLine);

        eventWriter.add(tab);
        eventWriter.add(eventFactory.createEndElement("", "", XML_LOAN_ELEMENT));
        eventWriter.add(endLine);
    }

    public String getResponse() {
        return response;
    }
}
