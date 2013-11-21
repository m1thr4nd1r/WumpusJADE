package wumpus;

import java.util.Random;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

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
	    
//		board = new String[][] { {"W","GS"," ","B"},
//								 {"S"," ","B","P"},
//								 {" ","B","P","B"},
//								 {"X"," ","B","P"},
//		  					   };
		
//	    Teste precisando matar o Wumpus e explorar espaco nao seguro
		board = new String[][] { {"B","B","BG","P"},
				 				 {"P","P","B","B"},
				 				 {"B","P","BS"," "},
				 				 {"X","BS","W","S"},
				   			   };
		
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
	
	private class WorldInformBehaviour extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;

		public WorldInformBehaviour(Agent a)
		{
			this.myAgent = a;
		}
		
		public void action() 
		{
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = receive(mt);
			
			while (msg != null)
			{
				if (msg.getConversationId().equals("Percept"))
				{
					// Determina onde se encontra a posi��o X do agente (contida na mensagem)
					String content = msg.getContent();
					int begin = 0;
					int end = content.indexOf('|');
					
					// Obtem e transforma a string contendo a posi��o X do agente em inteiro
					int x = Integer.parseInt(content.substring(begin , end));

					// Determina onde se encontra a posi��o Y do agente (contida na mensagem)					
					begin = end + 1;
					end = content.indexOf('@', begin);

					// Obtem e transforma a string contendo a posi��o X do agente em inteiro
					int y = Integer.parseInt(content.substring(begin, end));
					
					// Determina onde se encontra o id do agente, obtem-no e o transforma para inteiro					
					begin = end + 1;
					int agent = Integer.parseInt(content.substring(begin));
					
					System.out.println("\nPedido de informacao do agente " + Integer.toString(agent) + ":");
					System.out.println("X: " + (x) + " Y: " + (y) + " Conteudo: " + (board[y][x])); 

					// Cria uma mensagem de resposta, setando o performativo correto alem de
					// setar o conteudo (correspondente a informacao contida no tabuleiro)
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent(board[y][x]);
					
					send(reply);
				}
				else if (msg.getConversationId().equals("Shoot"))
				{
					String content = msg.getContent();
					int begin = 0;
					int end = content.indexOf('|');
					
					int x = Integer.parseInt(content.substring(begin , end));
					
					begin = end + 1;
					end = content.indexOf('|', begin);
					
					int y = Integer.parseInt(content.substring(begin, end));
					
					begin = end + 1;
					String orientation = content.substring(begin);

					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.INFORM);
					
					if (killedWumpus(x, y, orientation))
						reply.setContent("True");
					else
						reply.setContent("False");
					
					send(reply);
				}
				else if (msg.getConversationId().equals("Gold Grabbed"))
				{
					String content = msg.getContent();
					int begin = 0;
					int end = content.indexOf('|');
					
					int x = Integer.parseInt(content.substring(begin , end));
					
					begin = end + 1;
					end = content.indexOf('|', begin);
					
					int y = Integer.parseInt(content.substring(begin, end));
					
					board[y][x].replace("G", "");
				}
				msg = receive();
			}
		}
	
		// Verifica se o Wumpus foi morto pelo Agente
		public boolean killedWumpus(int x, int y, String orientation)
	    {
			int i;
			
			switch (orientation)
	        {
	            case "Up":
	                        i = y;
	                        while (i > 0)
	                        {    
	                            if (board[i][x].contains("W"))
	                            {
	                            	board[i][x].replace("W", "");
	                                return true;
	                            }
	                            i--;
	                        }
	                        break;
	            case "Down":
	                        i = y;
	                        while (i < 4)
	                        {
	                            if (board[i][x].contains("W"))
	                            {
	                            	board[i][x].replace("W", "");
	                                return true;
	                            }
	                            i++;
	                        }
	                        break;
	            case "Left":
	                        i = x;
	                        while (i > 0)
	                        {
	                        	if (board[y][i].contains("W"))
	                        	{
	                        		board[y][i].contains("W");
	                                return true;
	                        	}
	                            i--;
	                        }
	                        break;
	            case "Right":
	                        i = x;
	                        while (i < 4)
	                        {
	                        	if (board[y][i].contains("W"))
	                        	{
	                        		board[y][i].contains("W");
	                                return true;
	                        	}
	                            i++;
	                        }
	                        break;
	    	}
			
			return false;
	    }
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