import java.util.ArrayList;

public class Space {

    private         int                     colour;
    private         boolean                 isEncapsulated;
    private         int                     i;
    private         int                     j;

    public Space(int colour, boolean isEncapsulated, int x, int y) {
        this.colour = colour;
        this.isEncapsulated = isEncapsulated;
        i = x;
        j = y;
    }

    public String toString() {
        return Integer.toString(this.colour) /*+ " " + isEncapsulated*/;
    }



    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }


    public int getColour() {
        return colour;
    }

    public void setColour(int colour) {
        this.colour = colour;
    }

    public boolean isEncapsulated() {
        return isEncapsulated;
    }

    public void setEncapsulated(boolean encapsulated) {
        isEncapsulated = encapsulated;
    }



}
