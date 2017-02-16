package editor; 
import java.util.LinkedList;
import javafx.scene.text.Text;

public class TextLinkedList<T extends Text> extends LinkedList {
	private LinkedList<Node> list;
	private Node node;
	private int size;
	private int cursorIndex;
	private int index;
	private double enterHeight;

	private class Node<T extends Text> {
		public double xAxis;
		public double yAxis;
		public int i;
		public double height;
		public Text t;

		public Node(Text text) {
			this.t = text;
			if (size == 0) {
				this.xAxis = t.getX() + Math.round(t.getLayoutBounds().getWidth());
				this.yAxis = t.getY() + enterHeight;
				this.i = index;
			} else {
				this.xAxis = list.get(index - 1).getX() + Math.round(t.getLayoutBounds().getWidth());
				this.yAxis = Math.round(enterHeight);
				this.i = index;
			}
			this.height = Math.round(t.getLayoutBounds().getHeight());
		}

		public Node() {
			this.t = null;
			this.xAxis = 5;
			this.yAxis = 0;
			this.i = 0;
			this.height = 0;
		}

		public double getX() {
			return xAxis;
		}

		public double getY() {
			return yAxis;
		}
	}

	public TextLinkedList() {
		node = new Node();
		list = new LinkedList<Node>();
		size = 0;
		cursorIndex = 0;
		enterHeight = 0;
	}

	public void add(Text object) {
		Node newNode = new Node(object);
		list.add(newNode);
		this.cursorIndex ++;
		size ++;
		index ++;
	}

	public void add(Text object, int i) {
		Node newNode = new Node(object);
		if (isEnter(object)) {
			enterHeight += Math.round(object.getLayoutBounds().getHeight() / 2);
			newNode.xAxis = 5;
			newNode.yAxis = enterHeight;
		}
		list.add(i, newNode);
		this.cursorIndex = i + 1;
		size ++;
		index ++;
	}

	public void addFirst(Text object) {
		Node newNode = new Node(object);
		list.addFirst(newNode);
		this.cursorIndex = 1; //might need to change later
		size ++;
		index ++;
	}

	public void addLast(Text object) {
		Node newNode = new Node(object);
		list.addLast(newNode);
		this.cursorIndex = size - 1; //might need to change later
		size ++;
		index ++;
	}


	public Text remove(int i) {
		this.cursorIndex --;
		this.size --;
		this.index --;
		Text text = list.remove(i).t;
		if (isEnter(text)) {
			enterHeight -= Math.round(text.getLayoutBounds().getHeight() / 2);
		}
		return text;
	}

	public Text removeLast() {
		size --;
		this.cursorIndex = this.size - 1;
		index --;
		return list.removeLast().t;
	}

	public Text get(int i) {
		return list.get(i).t;
	}

	public double getX(int i) {
		return list.get(i).xAxis;
	}

	public double getY(int i) {
		return list.get(i).yAxis;
	}

	public double getHeight(int i) {
		return list.get(i).height;
	}

	public int getIndex() {
		return this.index;
	}

	public int getCursorIndex() {
		if (this.cursorIndex < 1) {
			return 0;
		}
		return this.cursorIndex;
	}

	public boolean isEmpty() {
		return list.size() == 0;
	}

	public int size() {
		return list.size();
	}

	public void cursorLeft() {
		if (this.cursorIndex <= -1) {
			this.cursorIndex = -1;
		}
		this.cursorIndex --;
	}

	public void cursorRight() {
		this.cursorIndex ++;
	}

	public void changeY(double y) {
		node.yAxis = y;
	}

	public void setCursorIndex(int i) {
		this.cursorIndex = i;
	}

	public void print() {
		for (Node n:list) {
			System.out.print(n.t.getText());
			//System.out.print(cursorIndex + " ");
		}
		System.out.println(" ");
	}

	private boolean isEnter(Text t) {
		if (t.getText().equals("\r")) {
			return true;
		}
		return false;
	}

	private void enterAdd(Text object, int i) {
		Node newNode = new Node(object);
		newNode.xAxis = 5;
		newNode.yAxis = list.get(cursorIndex - 1).getY() + object.getLayoutBounds().getHeight() / 2;
		newNode.height = object.getLayoutBounds().getHeight() / 2;
		list.add(i, newNode);
		this.cursorIndex ++;
		size ++;
		index ++;
	}

	public double getEnterHeight() {
		return this.enterHeight;
	}

	public void update(int i) {
		list.get(i).xAxis = list.get(i).getX() - Math.round(list.get(i).t.getLayoutBounds().getWidth());
		list.get(i).yAxis = list.get(i).getY();
		list.get(i).i --;
	}

	public void printCursor() {
		if (cursorIndex <= 0) {
			System.out.println("5 , 0");
		} else {
			System.out.println(list.get(cursorIndex - 1).getX() + " , " + list.get(cursorIndex - 1).getY());
		}
	}
}