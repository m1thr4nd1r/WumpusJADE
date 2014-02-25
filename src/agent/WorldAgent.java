package agent;

import java.util.Random;

import behaviour.WorldInformBehaviour;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class WorldAgent extends Agent {
	
	private static final long serialVersionUID = 1L;
	public int board_size;
    public int turns;
	private String board[][];
    
	protected void setup()
	{
		board_size = 4;
	    turns = 1;
//		board =	new String[][] { {"B"," ","B"," "},
//					         	 {"P","B","P","B"},
//					         	 {"B"," ","BSG","P"},
//					         	 {"X","S","W","SB"},
//					      		};
		
//		board = new String[][] { {"S"," ","B","P"},
//								 {"W","SBG","P","B"},
//						 		 {"S"," ","B"," "},
//						 		 {"X","B","P","B"},
//           					   };
	    
		board = new String[][] { {"W","GS"," ","B"},
								 {"S"," ","B","P"},
								 {" ","B","P","B"},
								 {"X"," ","B","P"},
		  					   };
		
//	    Teste precisando matar o Wumpus e explorar espaco nao seguro
//		board = new String[][] { {"B","B","BG","P"},
//				 				 {"P","P","B","B"},
//				 				 {"B","P","BS"," "},
//				 				 {"X","BS","W","S"},
//				   			   };
		
//	    board = new String[board_size][board_size];
//		
//        for (int y = 0; y < board_size; y++)
//            for (int x = 0; x < board_size; x++)
//                board[y][x] = " ";
//        
//        board[3][0] = "X";
//        
//        generate('G');
//        
//        generate('W');
//        
//        for (int i = 1; i < board_size; i++)
//            generate('P');
        
		System.out.println("O mundo inicializado eh: ");
        printBoard();
        
     // Registrando o Agente no DF
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
		dfd.setName(getAID());
		sd.setType("world");
		sd.setName("JADE-Wumpus");
		dfd.addServices(sd);
		
		try {
			DFService.register(this, dfd);
			addBehaviour(new WorldInformBehaviour(this));
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
	
	public void generate(char code)
    {
        int x,y;
        Random gen = new Random();
        
        do
        {
            x = gen.nextInt(board_size);
            y = gen.nextInt(board_size);
        } while ((((code == 'W') || (code == 'P')) && (((y == board_size-1) && (x == 1)) || ((y == board_size-2) && (x == 0)))) ||
                ((y == board_size-1) && (x == 0)) || 
                (!board[y][x].equals(" ")));
        
        board[y][x] += code;
        
        //	Mark Neighbours
        
        if (code == 'P')
        {
            if (y > 0)
                if (!board[y-1][x].contains("B"))
                    board[y-1][x] += "B";
            if (y < board_size - 1)
                if (!board[y+1][x].contains("B"))
                    board[y+1][x] += "B";
            if (x > 0)
                if (!board[y][x-1].contains("B"))
                    board[y][x-1] += "B";
            if (x < board_size - 1) 
                if (!board[y][x+1].contains("B"))
                    board[y][x+1] += "B";
        }
        else if (code == 'W')
        {
            if (y > 0)
                if (!board[y-1][x].contains("W"))
                    board[y-1][x] += "S";
            if (y < board_size - 1)
                if (!board[y+1][x].contains("W"))
                    board[y+1][x] += "S";
            if (x > 0)
                if (!board[y][x-1].contains("W"))
                    board[y][x-1] += "S";
            if (x < board_size - 1) 
                if (!board[y][x+1].contains("W"))
                    board[y][x+1] += "S";
        }
        else if (code == 'G')
            if (!board[y][x].contains("G"))
                board[y][x] += "G";        
    }
	
	public String[][] getBoard() {
		return board;
	}

	public void setBoard(String content, int x, int y) {
		this.board[x][y] = content;
	}
	
	public void printBoard()
    {
        for (int i = 0 ; i < board_size; i++)
        {
            for (int j = 0; j < board_size; j++)
                if (j != 0) 
                    System.out.print("  |  " + board[i][j]);
                else
                    System.out.print(" " + board[i][j] + " ");
            System.out.println();
        }
        System.out.println();
    }	
}