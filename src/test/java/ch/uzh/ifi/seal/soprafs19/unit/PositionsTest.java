package ch.uzh.ifi.seal.soprafs19.unit;
import ch.uzh.ifi.seal.soprafs19.utilities.Position;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PositionsTest {

    @Test
    public void testValidPosition() throws Exception
    {
        Position position = new Position(-1, 0, 0);
        assertFalse(position.hasValidAxis());

        position = new Position(4, 4, 3);
        assertTrue(position.hasValidAxis());
    }

    @Test
    public void testValidDirections() throws Exception
    {
        Position position = new Position(1, 0, 0);
        Position offset = new Position(0,0,0);

        assertTrue(position.isEastTo(offset));
        assertFalse(position.isSouthTo(offset));
        assertFalse(position.isWestTo(offset));
        assertFalse(position.isNorthTo(offset));
    }
}

