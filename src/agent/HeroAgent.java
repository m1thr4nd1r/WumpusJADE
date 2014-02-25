package agent;

import behaviour.ActBehaviour;
import behaviour.ListenBehaviour;
import model.Tile;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class HeroAgent extends Agent {

	private static final long serialVersionUID = 1L;
	
	private Tile board[][];
    private int x, y, previousSafeX, previousSafeY, id;
    private String orientation;
    private boolean arrow, gold, alive, safe, wumpusLocation;
    private AID friend;
	
    protected void setup()
	{	
    	// Setando o id do agente
        int end = getName().indexOf('@');
		this.id = Integer.parseInt(getName().substring(end - 1, end));
    	
    	// Inicializando Agente (orientacao sempre a direita e
    	// comecando da posicao inicial)
    	this.y = 3;
        this.x = 0;
        
        if (id == 0)
        	this.orientation = "Right";
        else
        	this.orientation = "Up";
        
    	// Inicializando atributos boleanos
        this.arrow = true;
        this.gold = false;
        this.alive = true;
        this.safe = true;
        this.wumpusLocation = false;
        
        // Inicializando a ultima posicao segura (inicialmente zerada)
        this.previousSafeX = -1;
        this.previousSafeY = -1;
        
		// Criando o tabuleiro do jogo
        this.board = new Tile[4][4];
        for (int j = 0; j < 4; j++)
            for (int i = 0; i < 4; i++)
                this.board[j][i] = new Tile();
        
        // Setando o inicio como visitado
        this.board[this.y][this.x].setVisited(true);
        
        // Setando os vizinhos do inicio como seguros
        this.board[this.y][this.x].setWumpus(0);
        this.board[this.y][this.x].setPit(0);
        this.board[this.y-1][this.x].setWumpus(0);
        this.board[this.y-1][this.x].setPit(0);
        this.board[this.y][this.x+1].setWumpus(0);
        this.board[this.y][this.x+1].setPit(0);
        
        // Registrando o Agente no DF
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
		dfd.setName(getAID());
		sd.setType("hero");
		sd.setName("JADE-Wumpus");
		dfd.addServices(sd);
		
		try {
			DFService.register(this, dfd);
			addBehaviour(new StartBehaviour(this));
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
    }
    
    protected void takeDown() 
    {
       try { DFService.deregister(this); }
       catch (Exception e) {}
    }
    
    public AID getFriend() {
		return friend;
	}

	public void setFriend(AID friend) {
		this.friend = friend;
	}

    public Tile[][] getBoard() {
		return board;
	}
    
    public void setBoard(Tile content, int x, int y) {
		this.board[x][y] = content;
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

	public boolean getWumpusLocation() {
		return wumpusLocation;
	}

	public void setWumpusLocation(boolean wumpusLocation) {
		this.wumpusLocation = wumpusLocation;
	}

	public void setArrow(boolean arrow) {
		this.arrow = arrow;
	}

	public boolean getArrow() {
		return arrow;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	public boolean isAlive() {
		return alive;
	}

	public boolean getGold() {
		return gold;
	}

	public void setGold(boolean gold) {
		this.gold = gold;
	}

	public int getPreviousSafeX() {
		return previousSafeX;
	}

	public void setPreviousSafeX(int previousSafeX) {
		this.previousSafeX = previousSafeX;
	}

	public int getPreviousSafeY() {
		return previousSafeY;
	}

	public void setPreviousSafeY(int previousSafeY) {
		this.previousSafeY = previousSafeY;
	}

	public int getId() {
		return id;
	}

	public String getOrientation() {
		return orientation;
	}

	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}

	public boolean isSafe() {
		return safe;
	}

	public void setSafe(boolean safe) {
		this.safe = safe;
	}
    
    public void printAction(String action)
    {
        System.out.println();
        System.out.println("Position: X = " + this.x + " Y = " + this.y);
        System.out.println("Orientation: " + this.orientation);
        System.out.println(action);
        System.out.println("------------");
    }
    
	public void printBoard()
	{
		System.out.println("   S B G b s   V P W     S B G b s   V P W     S B G b s   V P W     S B G b s   V P W");
		for (int i = 0 ; i < 4; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				System.out.print(" ");
				if (j != 0)
					System.out.print("| ");
  
				System.out.print("[ ");
  
				for (int k = 0; k < 5; k++)
					if (this.board[i][j].getPercept(k))
						System.out.print("T ");
					else
						System.out.print("F ");
  
				System.out.print("] ");
  
				if (i == y && j == x)
					System.out.print("H ");
				else if (this.board[i][j].isVisited())
					System.out.print("T ");
				else
					System.out.print("F ");
                      
				System.out.print(this.board[i][j].getPit() + " " + this.board[i][j].getWumpus());
			}
			System.out.println();
		}
	}
    
    private class StartBehaviour extends SimpleBehaviour
    {
		private static final long serialVersionUID = 1L;
		private boolean start = false;
		
		public StartBehaviour(Agent a)
		{
			this.myAgent = (HeroAgent) a;
		}
		
		public void action()
		{
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("hero");
			template.addServices(sd);
			try {
				DFAgentDescription[] agents = DFService.search(myAgent, template); 
				if (agents.length == 2)
				{
					for (int i = 0; i < agents.length; i++)
						if (!this.myAgent.getName().equals(agents[i].getName().getName()))
							friend = agents[i].getName();
					start = true;
				}
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
		}
		
		public boolean done()
		{
			return start;
		}
		
		public int onEnd()
		{
			myAgent.addBehaviour(new ListenBehaviour(myAgent));
			myAgent.addBehaviour(new ActBehaviour(myAgent));
			return 0;
		}
    }
}