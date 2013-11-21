package wumpus;
public class Node {
    private int f,g,h,x,y;
    private String orientation;
    private String action;
    private Node parent;

    public Node(int x, int y, String orientation, String action) {
    	this(x, y, orientation);
    	if (action.equals("Left") || action.equals("Right"))
    	{
	    	this.rotate(action);
	    	this.action = "Turning " + action;    	
    	}
    	else
    		this.action = action;
    }
    
    public Node(int x, int y, String orientation) {
        this.x = x;
        this.y = y;
        
        this.orientation = orientation;
        this.action = "";
        
        this.f = 0;
        this.h = 0;
        this.g = 0;
        
        parent = null;
    }   

    public int getF() {
        return f;
    }

    public void setF() {
        this.f = this.g + this.h;
    }

    public int getG() {
        return g;
    }

    public void setG(int g) {
        this.g = g;
    }
    
    public int getH() {
        return h;
    }

    public int setH(int x, int y)
    {
        this.h = Math.abs(this.y - y) + Math.abs(this.x - x);
        return this.h;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Node getParent() {
        return parent;
    }
    
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    
    public String getAction() {
    	return action;
    }
    
    public String getOrientation() {
        return orientation;
    }
    
    public void rotate(String direction)
    {
        switch (this.orientation)
        {
            case "Up":
                        this.orientation = (direction.equals("Right"))? "Right" : "Left";
                        break;
            case "Down":
                        this.orientation = (direction.equals("Right"))? "Left" : "Right";
                        break;
            case "Left":
                        this.orientation = (direction.equals("Right"))? "Up" : "Down";
                        break;
            case "Right":
                        this.orientation = (direction.equals("Right"))? "Down" : "Up";
                        break;
        }
    }
    
    public int rotationCost(Node n)
    {
    	int left = 0, right = 0;
    	
    	Node cp_left = new Node(n.getX(), n.getY(), n.getOrientation());
    	Node cp_right = new Node(n.getX(), n.getY(), n.getOrientation());
    	
    	if (n.getOrientation().equals(this.orientation))
    	{
    		this.action = "Moving Foward";
    		return 0;
    	}
    	else
	    	for (int i = 0 ; i < 4; i++)
	    	{
	    		if (cp_left.getOrientation().equals(this.orientation))
	    		{    			
	    			this.action = "Turning Left";
	    			return left;    			
	    		}
	    		else
	    		{
	    			left++;
	    			cp_left.rotate("Left");
	    		}
	    		
	    		if (cp_right.getOrientation().equals(this.orientation))
	    		{
	    			this.action = "Turning Right";
	    			return right;
	    		}
	    		else
	    		{
	    			right++;
	    			cp_right.rotate("Right");
	    		}
	    	}
    	
    	return - 1; // Error code
    }
    
    @Override
    public boolean equals(Object n)
    {
        Node a = (Node) n;
        return (this.y == a.getY() && this.x == a.getX() && this.f < a.getF());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.x;
        hash = 97 * hash + this.y;
        return hash;
    }
}