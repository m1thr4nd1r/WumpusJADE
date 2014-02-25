package model;
public class Tile {
    
    private boolean visited;
    private boolean percept[];
    private int pit;
    private int wumpus;

    public Tile() {
        this.visited = false;
        pit = 3;
        wumpus = 3;
        this.percept = new boolean[5];
        // percept[0] = Stench
        // percept[1] = Breeze
        // percept[2] = Glitter
        // percept[3] = Bump (Atingiu uma Parede)
        // percept[4] = Scream (Wumpus Morreu)
                
        for (short i = 0; i < 5; i++)        	
            this.percept[i] = false;        
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public boolean[] getAllPercept() {
        return percept;
    }
    
    public boolean getPercept(int i) {
        return percept[i];
    }
    
    public void setPercept(int i, boolean value)
    {
        this.percept[i] = value;
    }    

    public int getPit() {
        return pit;
    }

    public void setPit(int Pit) {
        this.pit = Pit;
    }

    public int getWumpus() {
        return wumpus;
    }

    public void setWumpus(int Wumpus) {
        this.wumpus = Wumpus;
    }
    
    public boolean isSafe()
    {
        return this.pit == 0 && this.wumpus == 0;
    }
}