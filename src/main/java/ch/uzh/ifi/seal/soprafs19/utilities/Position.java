package ch.uzh.ifi.seal.soprafs19.utilities;

import ch.uzh.ifi.seal.soprafs19.constant.Axis;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Position {


    private int x;
    private int y;
    private int z;

    public Position(){}

    public Position (int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    @JsonIgnore
    public boolean isFloor() {
        return this.getZ() == 0;
    }

    @JsonIgnore
    public boolean isCeil() {
        return this.getZ() == 3;
    }

    @JsonIgnore
    public boolean isNorthTo(Position position)
    {
        int[] offSet = calculatePositionOffSet(position);
        return offSet[0] == 0 && offSet[1] == 1;// && offSet[2] == 0;
    }

    @JsonIgnore
    public boolean isEastTo(Position position)
    {
        int[] offSet = calculatePositionOffSet(position);
        return offSet[0] == 1 && offSet[1] == 0; // && offSet[2] == 0;
    }

    @JsonIgnore
    public boolean isSouthTo(Position position)
    {
        int[] offSet = calculatePositionOffSet(position);
        return offSet[0] == 0 && offSet[1] == -1; // && offSet[2] == 0;
    }

    @JsonIgnore
    public boolean isWestTo(Position position)
    {
        int[] offSet = calculatePositionOffSet(position);
        return offSet[0] == -1 && offSet[1] == 0; // && offSet[2] == 0;
    }



    // Calculates the offSet between the position and an offset Position
    @JsonIgnore
    private int[] calculatePositionOffSet(Position offsetPosition)
    {
        // [0] = dx,
        // [1] = dy,
        // [2] = dz
        int[] offSet = new int[3];
        offSet[0] = this.x - offsetPosition.getX();
        offSet[1] = this.y - offsetPosition.getY();
        offSet[2] = this.z - offsetPosition.getZ();

        return offSet;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Position)) {
            return false;
        }
        Position position = (Position) o;

        return
                this.getX() == position.getX() &&
                this.getY() == position.getY() &&
                this.getZ() == position.getZ();
    }


    @Override
    public int hashCode() {
        int x = this.getX();
        int y = this.getY();
        int z = this.getZ();

        String hash = Integer.toString(x) + Integer.toString(y) + Integer.toString(z);

        return Integer.parseInt(hash);
    }

    public boolean hasValidAxis() {
        return Axis.XYAXIS.contains(this.getX()) &&
                Axis.XYAXIS.contains(this.getY()) &&
                Axis.ZAXIS.contains(this.getZ());
    }
}
