package net;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class MessageTest {

    @Test
    void roundTripKeepsAllFields() {
        Message original = new Message(MessageType.REQUEST, "B1", 7);   // arrange
        Message parsed = Message.parse(original.toLine());              // act
        assertEquals(MessageType.REQUEST, parsed.getType());            // assert
        assertEquals("B1", parsed.getSender());
        assertEquals(7, parsed.getTimestamp());
    }

    @Test
    void rejectsLineWithMissingField() {
        assertThrows(IllegalArgumentException.class, () -> Message.parse("REQUEST B1"));
    }

    @Test
    void toLineUsesSpaceSeparatedFormat(){
        Message m = new Message(MessageType.REQUEST, "B1", 7); //arrange
        String toLine = m.toLine(); //act
        assertEquals("REQUEST B1 7", toLine); //assert
    }

    @Test
    void controlMessageHasNoTimestamp(){
        Message m = new Message(MessageType.HELLO, "B1"); //arrange
        Message parsed = Message.parse(m.toLine()); //act
        assertEquals(MessageType.HELLO, parsed.getType()); //assert
        assertEquals(-1, parsed.getTimestamp());
    }

    @Test
    void rejectsUnknownType(){
        assertThrows(IllegalArgumentException.class, () -> Message.parse("FOO B1 7"));
    }

    @Test
    void rejectsNonNumericTimestamp(){
        assertThrows(IllegalArgumentException.class, () -> Message.parse("REQUEST B1 x"));
    }

    @Test
    void roundTripWorksForEveryType(){
        for(MessageType type : MessageType.values()){
            Message m = new Message(type, "B1", 1);
            Message parsed = Message.parse(m.toLine());
            assertEquals(type, parsed.getType());
        }
    }


}