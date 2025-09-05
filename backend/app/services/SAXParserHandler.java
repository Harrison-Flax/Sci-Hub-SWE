package services;


import models.Author;
import models.Paper;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;

public class SAXParserHandler extends DefaultHandler {
    public static List<Paper> records = new ArrayList<Paper>();

    // Stacks for storing the elements and objects.
    private Stack<String> elements = new Stack<String>();
    private Stack<Paper> objects = new Stack<Paper>();

    private String currentElement() {
        return elements.peek();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String value = new String(ch, start, length);
        if (value.length() == 0)
            return;

        if (currentElement().equalsIgnoreCase("booktitle")) {
            Paper record = objects.peek();
            record.setBookTitle(value);
        }
        if (currentElement().equalsIgnoreCase("title")) {
            Paper record = objects.peek();
            if (value == null || value == "")
                value = UUID.randomUUID() + "Auto Generated Title";
            record.setTitle(value);
        }
        if (currentElement().equalsIgnoreCase("author")) {
            Paper record = objects.peek();
            List<Author> authors = null;

            authors = record.getAuthors();

            String[] names = value.split(" ");
            Author author = new Author(value, "");
            author.setFirstName(names[0]);
            if (names.length > 1) {
                author.setLastName(names[1]);
            } else if (names.length > 2) {
                author.setLastName(names[1] + names[2]);
            }
            authors.add(author);


            record.setAuthors(authors);
        }
        if (currentElement().equalsIgnoreCase("editor")) {
            Paper record = objects.peek();
            record.setEditor(value);
        }
        if (currentElement().equalsIgnoreCase("publicationType")) {
            Paper record = objects.peek();
            record.setPublicationType(value);
        }
        if (currentElement().equalsIgnoreCase("publicationChannel")) {
            Paper record = objects.peek();
            if (value == null || value == "")
                value = UUID.randomUUID() + "Auto Generated PublicationChannel";
            record.setPublicationChannel(value);
        }
        if (currentElement().equalsIgnoreCase("date")) {
            Paper record = objects.peek();
            record.setDate(value);
        }
        if (currentElement().equalsIgnoreCase("year")) {
            Paper record = objects.peek();
            record.setYear(value);
        }
        if (currentElement().equalsIgnoreCase("month")) {
            Paper record = objects.peek();
            record.setMonth(value);
        }
        if (currentElement().equalsIgnoreCase("url")) {
            Paper record = objects.peek();
            record.setUrl(value);
        }
        if (currentElement().equalsIgnoreCase("publisher")) {
            Paper record = objects.peek();
            record.setPublisher(value);
        }
        if (currentElement().equalsIgnoreCase("address")) {
            Paper record = objects.peek();
            record.setAddress(value);
        }
        if (currentElement().equalsIgnoreCase("isbn")) {
            Paper record = objects.peek();
            record.setIsbn(value);
        }
        if (currentElement().equalsIgnoreCase("series")) {
            Paper record = objects.peek();
            record.setSeries(value);
        }
        if (currentElement().equalsIgnoreCase("school")) {
            Paper record = objects.peek();
            record.setSchool(value);
        }
        if (currentElement().equalsIgnoreCase("chapter")) {
            Paper record = objects.peek();
            record.setChapter(value);
        }
        if (currentElement().equalsIgnoreCase("volume")) {
            Paper record = objects.peek();
            record.setVolume(value);
        }
        if (currentElement().equalsIgnoreCase("number")) {
            Paper record = objects.peek();
            record.setNumber(value);
        }
        if (currentElement().equalsIgnoreCase("pages")) {
            Paper record = objects.peek();
            record.setPages(value);
        }

    }


    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        elements.push(qName);
        if ("inproceedings".equals(qName)) {
            Paper record = new Paper();
            objects.push(record);
            records.add(record);
        }
    }

    @Override
    public void endElement(String uri,
                           String localName, String qName) throws SAXException {

    }


}
