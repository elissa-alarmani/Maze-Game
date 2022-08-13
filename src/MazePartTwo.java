import java.util.*;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// represents an edge class
class Edge {
  Vertex from;
  Vertex to;
  int weight;

  // the constructor
  Edge(Vertex from, Vertex to, int weight) {
    this.from = from;
    this.to = to;
    this.weight = weight;
  }

  // overrides equal method to determine if this edge is equal to the given edge
  public boolean equals(Object o) {
    if (!(o instanceof Edge)) {
      return false;
    }
    Edge other = (Edge) o;
    return (this.from.equals(other.from) && this.to.equals(other.to) && this.weight == other.weight)
        || (this.from.equals(other.to) && this.to.equals(other.from)
            && this.weight == other.weight);
  }

  // overrides hashcode method
  public int hashCode() {
    return (this.from.location.x + this.from.location.y + this.to.location.x + this.to.location.y)
        * this.weight * 10000;
  }

  // draws an edge
  WorldImage drawEdge() {
    return new RectangleImage(9, 9, OutlineMode.SOLID, Color.gray);
  }

}

// compares the weights of two edges
class EdgeComparator implements Comparator<Edge> {
  EdgeComparator() {
  }

  // compares the edges by weight
  public int compare(Edge e1, Edge e2) {
    return e1.weight - e2.weight;
  }

}

//to represent a vertex
class Vertex {

  Posn location;

  ArrayList<Edge> outer;

  boolean isSearch;
  boolean isCorrect;

  // the constructor
  Vertex(Posn location, ArrayList<Edge> outer) {
    this.location = location;
    this.outer = outer;
    this.isSearch = false;
    this.isCorrect = false;
  }

  // overrides the equals method to determine if the given vertex is equal to this
  // vertex
  public boolean equals(Object object) {
    if (!(object instanceof Vertex)) {
      return false;
    }
    Vertex other = (Vertex) object;
    return (this.location.x == other.location.x && this.location.y == other.location.y
        && this.outer.equals(other.outer));
  }

  // overrides hashcode method
  public int hashCode() {
    return (((this.location.x * 5) - this.location.y) * 10000) - this.outer.hashCode();
  }

  // draws a cell of a vertex in the search pathway
  WorldImage drawSearch() {
    if (this.isSearch) {
      return new RectangleImage(9, 9, OutlineMode.SOLID, new Color(173, 216, 230));
    }
    else {
      return new EmptyImage();
    }
  }

  // draws a cell of a vertex in the reconstructed pathway in the last quarter
  WorldImage drawCorrect1() {
    if (this.isCorrect) {
      return new RectangleImage(9, 9, OutlineMode.SOLID, new Color(0, 0, 139));
    }
    else {
      return new EmptyImage();
    }
  }

  // draws a cell of a vertex in the reconstructed pathway in between the last
  // half and last quarter
  WorldImage drawCorrect2() {
    if (this.isCorrect) {
      return new RectangleImage(9, 9, OutlineMode.SOLID, Color.GREEN);
    }
    else {
      return new EmptyImage();
    }
  }

  // draws a cell of a vertex in the reconstructed pathway in between the first
  // half and first quarter
  WorldImage drawCorrect3() {
    if (this.isCorrect) {
      return new RectangleImage(9, 9, OutlineMode.SOLID, Color.ORANGE);
    }
    else {
      return new EmptyImage();
    }
  }

  // draws a cell of a vertex in the reconstructed pathway in the first quarter
  WorldImage drawCorrect4() {
    if (this.isCorrect) {
      return new RectangleImage(9, 9, OutlineMode.SOLID, Color.RED);
    }
    else {
      return new EmptyImage();
    }
  }

  // draw a cell
  WorldImage drawCellVertex() {
    return new RectangleImage(9, 9, OutlineMode.SOLID, Color.gray);
  }

}

// represents the maze solver game
class MazeGame extends World {

  int boardX;
  int boardY;

  ArrayList<ArrayList<Vertex>> board;
  ArrayList<Vertex> seenVertices;
  ArrayList<Vertex> pathVertices;

  HashMap<Vertex, Vertex> representatives;
  List<Edge> edgesInTree;
  List<Edge> worklist;

  int tick;
  int dCount;
  int bCount;
  int wDCount;
  int wBCount;

  // the constructor
  MazeGame(int boardX, int boardY) {
    this.boardX = boardX;
    this.boardY = boardY;
    this.board = this.makeBoard();
    this.edgesInTree = new ArrayList<Edge>();
    this.worklist = this.edgeSort(this.allEdges());
    this.minimumSpanningTree(this.worklist, this.board);
    this.seenVertices = new ArrayList<Vertex>();
    this.pathVertices = new ArrayList<Vertex>();
    this.connectEdges();
    this.tick = 0;
    this.dCount = 0;
    this.bCount = 0;
    this.wDCount = 0;
    this.wBCount = 0;

  }

  // creates a list of the vertices of the board
  ArrayList<ArrayList<Vertex>> makeBoard() {
    ArrayList<ArrayList<Vertex>> finalBoard = new ArrayList<ArrayList<Vertex>>();
    for (int i = 0; i < this.boardX; i++) {
      ArrayList<Vertex> temp = new ArrayList<Vertex>();
      for (int j = 0; j < this.boardY; j++) {
        ArrayList<Edge> edge = new ArrayList<Edge>();
        Vertex vertex = new Vertex(new Posn(i, j), edge);
        temp.add(vertex);
      }
      finalBoard.add(temp);
    }
    return finalBoard;
  }

  // sorts the list of edges by random weights according to the edge comparator
  List<Edge> edgeSort(ArrayList<Edge> e) {
    Collections.sort(e, new EdgeComparator());
    return e;
  }

  // replaces the vertex with the supplied vertex
  void union(HashMap<Vertex, Vertex> represent, Vertex vertex1, Vertex vertex2) {
    represent.put(vertex1, vertex2);
  }

  // finds the parent vertex at the given point
  Vertex find(HashMap<Vertex, Vertex> represent, Vertex vertex) {
    if (represent.get(vertex).equals(vertex)) {
      return represent.get(vertex);
    }
    else {
      return find(represent, represent.get(vertex));
    }
  }

  // connects the vertices and makes each vertex a representative, part of
  // kruskal's algo
  List<Edge> minimumSpanningTree(List<Edge> worklist, ArrayList<ArrayList<Vertex>> board) {
    HashMap<Vertex, Vertex> rep = new HashMap<Vertex, Vertex>();

    for (ArrayList<Vertex> list : board) {
      for (Vertex vertex : list) {
        rep.put(vertex, vertex);
      }
    }

    while (this.edgesInTree.size() < rep.size() - 1) {
      Edge curEdge = worklist.remove(0);

      if (!find(rep, curEdge.from).equals(find(rep, curEdge.to))) {
        this.edgesInTree.add(curEdge);
        union(rep, find(rep, curEdge.from), (find(rep, curEdge.to)));
      }
    }
    worklist.removeAll(this.edgesInTree);
    this.representatives = rep;
    return this.edgesInTree;
  }

  // connects the edges in the edgesInTree to the board
  void connectEdges() {
    for (Edge edge : this.edgesInTree) {
      edge.from.outer.add(edge);
      edge.to.outer.add(edge);
    }
  }

  // creates a list of edges that are possible to generate, creating the min
  // spanning tree
  ArrayList<Edge> allEdges() {

    ArrayList<Edge> answer = new ArrayList<Edge>();

    for (int i = 0; i < this.board.size(); i++) {
      for (int j = 0; j < this.board.get(i).size(); j++) {

        if (i < boardX - 1) {
          Vertex vertex1 = board.get(i).get(j);
          Vertex right = board.get(i + 1).get(j);
          Edge edgeR = new Edge(vertex1, right, new Random().nextInt(10000));
          answer.add(edgeR);
        }

        if (j < boardY - 1) {
          Vertex vertex1 = board.get(i).get(j);
          Vertex bottom = board.get(i).get(j + 1);
          Edge edgeB = new Edge(vertex1, bottom, new Random().nextInt(10000));
          answer.add(edgeB);
        }
      }
    }
    return answer;
  }

  // makes the scene
  public WorldScene makeScene() {

    WorldScene finalScene = new WorldScene(1500, 900);

    // draws the title
    WorldImage title = new TextImage("Maze Solver", 75, FontStyle.BOLD_ITALIC, Color.GREEN);
    finalScene.placeImageXY(title, 800, 650);

    // draws the DFS instructions
    WorldImage directionD1 = new TextImage("Press 'D' to solve the maze", 20, FontStyle.REGULAR,
        Color.BLACK);
    WorldImage directionD2 = new TextImage("via depth-first search", 20, FontStyle.REGULAR,
        Color.BLACK);

    finalScene.placeImageXY(directionD1, 800, 700);
    finalScene.placeImageXY(directionD2, 800, 718);

    // draws the BFS instructions
    WorldImage directionS1 = new TextImage("Press 'B' to solve the maze", 20, FontStyle.REGULAR,
        Color.BLACK);
    WorldImage directionS2 = new TextImage("via breadth-first search", 20, FontStyle.REGULAR,
        Color.BLACK);

    finalScene.placeImageXY(directionS1, 800, 737);
    finalScene.placeImageXY(directionS2, 800, 755);

    // draws the restart instructions
    WorldImage directionRestart = new TextImage("Press 'R' to restart the maze", 20,
        FontStyle.REGULAR, Color.BLACK);
    finalScene.placeImageXY(directionRestart, 800, 823);

    // draws the timer
    WorldImage timer = new TextImage(
        "Timer: " + Integer.toString((int) this.tick / 20) + " seconds", 20, Color.BLACK);
    WorldImage finalTimer = new OverlayImage(timer,
        new RectangleImage(200, 40, OutlineMode.SOLID, Color.PINK));

    finalScene.placeImageXY(finalTimer, 1100, 730);

    // draws the DFS move count
    WorldImage dCount = new TextImage("DFS Count: " + Integer.toString(this.dCount), 20,
        Color.PINK);
    finalScene.placeImageXY(dCount, 500, 730);

    // draws the BFS moves count
    WorldImage bCount = new TextImage("BFS Count: " + Integer.toString(this.bCount), 20,
        Color.PINK);
    finalScene.placeImageXY(bCount, 500, 700);

    // draws the solution path count
    WorldImage pCount = new TextImage(
        "Solution Path Count: " + Integer.toString(this.pathVertices.size()), 20, Color.PINK);
    finalScene.placeImageXY(pCount, 360, 670);

    // draws the DFS wrong move count
    WorldImage wDCount = new TextImage("Wrong DFS Moves: " + Integer.toString(this.wDCount), 20,
        Color.PINK);
    finalScene.placeImageXY(wDCount, 200, 730);

    // draws the BFS wrong moves count
    WorldImage wBCount = new TextImage("Wrong BFS Moves: " + Integer.toString(this.wBCount), 20,
        Color.PINK);
    finalScene.placeImageXY(wBCount, 200, 700);

    // draws if BFS takes less moves
    if (this.bCount < this.dCount && this.bCount != 0 && this.dCount != 0) {
      WorldImage bWin = new TextImage(
          "BFS takes " + Integer.toString(this.dCount - this.bCount) + " less moves!", 20,
          Color.PINK);
      finalScene.placeImageXY(bWin, 360, 640);
    }

    // draws if DFS takes less moves
    if (this.dCount < this.bCount && this.dCount != 0 && this.bCount != 0) {
      WorldImage bWin = new TextImage(
          "DFS takes " + Integer.toString(this.bCount - this.dCount) + " less moves!", 20,
          Color.PINK);
      finalScene.placeImageXY(bWin, 360, 640);
    }

    // draw the grid
    for (ArrayList<Vertex> list : this.board) {
      for (Vertex vertex : list) {
        finalScene.placeImageXY(vertex.drawCellVertex(), vertex.location.x * 10 + 5,
            vertex.location.y * 10 + 5);
      }
    }

    // draws the edges
    for (Edge edge : this.edgesInTree) {
      finalScene.placeImageXY(edge.drawEdge(), (edge.to.location.x + edge.from.location.x) * 5 + 5,
          (edge.to.location.y + edge.from.location.y) * 5 + 5);
    }

    // draws the start
    WorldImage startingBlock = new RectangleImage(9, 9, OutlineMode.SOLID, Color.GREEN);

    finalScene.placeImageXY(startingBlock, 5, 5);

    // draws the end
    WorldImage endingBlock = new RectangleImage(9, 9, OutlineMode.SOLID, new Color(147, 112, 219));
    finalScene.placeImageXY(endingBlock, boardX * 10 - 5, boardY * 10 - 5);

    // draws the seen
    for (Vertex v : this.seenVertices) {
      finalScene.placeImageXY(v.drawSearch(), v.location.x * 10 + 5, v.location.y * 10 + 5);
    }

    // draws the path
    if (this.tick >= this.seenVertices.size()) {
      for (Vertex v : this.pathVertices) {
        if (this.pathVertices.indexOf(v) <= this.pathVertices.size() * .25) {
          finalScene.placeImageXY(v.drawCorrect1(), v.location.x * 10 + 5, v.location.y * 10 + 5);
        }
        if (this.pathVertices.indexOf(v) <= this.pathVertices.size() * .50
            && this.pathVertices.indexOf(v) >= this.pathVertices.size() * .25) {
          finalScene.placeImageXY(v.drawCorrect2(), v.location.x * 10 + 5, v.location.y * 10 + 5);
        }
        if (this.pathVertices.indexOf(v) <= this.pathVertices.size() * .75
            && this.pathVertices.indexOf(v) >= this.pathVertices.size() * .50) {
          finalScene.placeImageXY(v.drawCorrect3(), v.location.x * 10 + 5, v.location.y * 10 + 5);
        }
        if (this.pathVertices.indexOf(v) <= this.pathVertices.size()
            && this.pathVertices.indexOf(v) >= this.pathVertices.size() * .75) {
          finalScene.placeImageXY(v.drawCorrect4(), v.location.x * 10 + 5, v.location.y * 10 + 5);
        }
      }
    }

    return finalScene;
  }

  // moves the player and also switches from breath-first and depth-first search.
  public void onKeyEvent(String ke) {

    if (ke.equals("b")) {
      this.tick = 0;
      this.seenVertices.clear();
      this.pathVertices.clear();
      this.solveMaze(ke);
      this.bCount = this.seenVertices.size();
      this.wBCount = this.bCount - this.pathVertices.size();
    }
    if (ke.equals("d")) {
      this.tick = 0;
      this.seenVertices.clear();
      this.pathVertices.clear();
      this.solveMaze(ke);
      this.dCount = this.seenVertices.size();
      this.wDCount = this.dCount - this.pathVertices.size();
    }

    if (ke.equals("r")) {
      MazeGame restart = new MazeGame(this.boardX, this.boardY);
      this.board = restart.board;
      this.representatives = restart.representatives;
      this.edgesInTree = restart.edgesInTree;
      this.worklist = restart.worklist;
      this.seenVertices = restart.seenVertices;
      this.pathVertices = restart.pathVertices;
      this.tick = restart.tick;
      this.dCount = restart.dCount;
      this.bCount = restart.bCount;
    }
  }

  // solves the maze BFS or DFS given the string provided
  void solveMaze(String ke) {
    HashMap<Vertex, Vertex> cameFromEdge = new HashMap<Vertex, Vertex>();
    ArrayList<Vertex> worklist = new ArrayList<Vertex>();
    worklist.add(this.board.get(0).get(0));
    this.seenVertices.clear();

    while (worklist.size() > 0) {
      Vertex next = worklist.remove(0);
      Vertex last = this.board.get(this.board.size() - 1).get(this.board.get(0).size() - 1);
      if (next.equals(last)) {
        this.reconstruct(cameFromEdge, next);
        return;
      }
      for (Edge edge : next.outer) {
        if (!this.seenVertices.contains(edge.to) && next.equals(edge.from)) {
          if (ke.equals("d")) {
            worklist.add(0, edge.to);
          }

          if (ke.equals("b")) {
            worklist.add(edge.to);
          }

          this.seenVertices.add(next);
          cameFromEdge.put(edge.to, next);
        }
        else if (!this.seenVertices.contains(edge.from) && next.equals(edge.to)) {
          if (ke.equals("d")) {
            worklist.add(0, edge.from);
          }

          if (ke.equals("b")) {
            worklist.add(edge.from);
          }

          this.seenVertices.add(next);
          cameFromEdge.put(edge.from, next);
        }
      }
    }
  }

  // reconstructs the path from the end to the beginning
  public void reconstruct(HashMap<Vertex, Vertex> cameFromEdge, Vertex next) {
    this.pathVertices.add(this.board.get(this.board.size() - 1).get(this.board.get(0).size() - 1));
    Vertex inital = this.board.get(0).get(0);
    while (inital != next) {
      this.pathVertices.add(cameFromEdge.get(next));
      next = cameFromEdge.get(next);
    }
  }

  // onTick method
  public void onTick() {
    if (this.tick > -1) {
      this.tick += 1;
    }

    if (this.pathVertices.size() > 0 && this.tick > this.seenVertices.size()) {
      if (this.pathVertices.size() > this.tick - this.seenVertices.size()) {
        Vertex path = this.pathVertices.get(this.tick - this.seenVertices.size());
        path.isCorrect = true;
      }
    }

    if (this.seenVertices.size() > 0) {
      if (this.tick < this.seenVertices.size()) {
        Vertex seen = this.seenVertices.get(this.tick);
        seen.isSearch = true;
      }
    }

  }

}

//tests and examples
class ExamplesMaze {

  void testMazeGame(Tester t) {
    MazeGame game = new MazeGame(20, 20);
    game.bigBang(1500, 900, .05);
  }

  MazeGame mazeGame;
  Vertex a;
  Vertex b;
  Vertex c;
  Vertex d;
  Vertex e;
  Vertex f;

  Edge aToB;
  Edge aToE;
  Edge bToF;
  Edge bToC;
  Edge bToE;
  Edge eToC;
  Edge cToD;
  Edge fToD;

  ArrayList<Edge> listEdge;

  ArrayList<ArrayList<Vertex>> board1;

  ArrayList<Vertex> list1;

  EdgeComparator comp;

  HashMap<Vertex, Vertex> hashMap;

  // initData
  void initData() {
    mazeGame = new MazeGame(40, 40);

    a = new Vertex(new Posn(0, 0), new ArrayList<Edge>());
    b = new Vertex(new Posn(0, 1), new ArrayList<Edge>());
    c = new Vertex(new Posn(0, 2), new ArrayList<Edge>());
    d = new Vertex(new Posn(1, 0), new ArrayList<Edge>());
    e = new Vertex(new Posn(1, 1), new ArrayList<Edge>());
    f = new Vertex(new Posn(1, 2), new ArrayList<Edge>());

    aToB = new Edge(this.a, this.b, 30);
    aToE = new Edge(this.a, this.e, 50);
    bToF = new Edge(this.b, this.f, 50);
    bToC = new Edge(this.b, this.c, 40);
    bToE = new Edge(this.b, this.e, 35);
    eToC = new Edge(this.e, this.c, 15);
    cToD = new Edge(this.c, this.d, 25);
    fToD = new Edge(this.f, this.d, 50);

    comp = new EdgeComparator();

    hashMap = new HashMap<Vertex, Vertex>();

    listEdge = new ArrayList<Edge>(Arrays.asList(this.aToB, this.aToE, this.bToF, this.bToC,
        this.bToE, this.eToC, this.cToD, this.fToD));

  }

  // tests the drawSearch method
  void testDrawSearch(Tester t) {
    initData();

    // testing drawSearch() on vertices where isSearch is false
    t.checkExpect(this.a.drawSearch(), new EmptyImage());

    // change's vertices isSearch to true
    this.a.isSearch = true;

    // testing drawSearch() on vertices where isSearch is true
    t.checkExpect(this.a.drawSearch(),
        new RectangleImage(9, 9, OutlineMode.SOLID, new Color(173, 216, 230)));
  }

  // tests the drawCorrect1 method
  void testDrawCorrect1(Tester t) {
    initData();

    // testing drawCorrect() on vertices where isCorrect is false
    t.checkExpect(this.a.drawCorrect1(), new EmptyImage());

    // change's vertices isCorrect to true
    this.a.isCorrect = true;

    // testing drawCorrect() on vertices where isSearch is true
    t.checkExpect(this.a.drawCorrect1(),
        new RectangleImage(9, 9, OutlineMode.SOLID, new Color(0, 0, 139)));
  }

  // tests the drawCorrect2 method
  void testDrawCorrect2(Tester t) {
    initData();

    // testing drawCorrect() on vertices where isCorrect is false
    t.checkExpect(this.a.drawCorrect2(), new EmptyImage());

    // change's vertices isCorrect to true
    this.a.isCorrect = true;

    // testing drawCorrect() on vertices where isSearch is true
    t.checkExpect(this.a.drawCorrect2(), new RectangleImage(9, 9, OutlineMode.SOLID, Color.GREEN));
  }

  // tests the drawCorrect3 method
  void testDrawCorrect3(Tester t) {
    initData();

    // testing drawCorrect() on vertices where isCorrect is false
    t.checkExpect(this.a.drawCorrect3(), new EmptyImage());

    // change's vertices isCorrect to true
    this.a.isCorrect = true;

    // testing drawCorrect() on vertices where isSearch is true
    t.checkExpect(this.a.drawCorrect3(), new RectangleImage(9, 9, OutlineMode.SOLID, Color.ORANGE));
  }

  // tests the drawCorrect4 method
  void testDrawCorrect4(Tester t) {
    initData();

    // testing drawCorrect() on vertices where isCorrect is false
    t.checkExpect(this.a.drawCorrect4(), new EmptyImage());

    // change's vertices isCorrect to true
    this.a.isCorrect = true;

    // testing drawCorrect() on vertices where isSearch is true
    t.checkExpect(this.a.drawCorrect4(), new RectangleImage(9, 9, OutlineMode.SOLID, Color.RED));
  }

  // tests onKeyEvent method
  void testOnKeyEvent(Tester t) {
    initData();
    this.mazeGame.onKeyEvent("b");
    t.checkExpect(this.mazeGame.seenVertices.size() > 0, true);
    t.checkExpect(this.mazeGame.pathVertices.size() > 0, true);
    this.mazeGame.onKeyEvent("r");
    t.checkExpect(this.mazeGame.seenVertices.size() == 0, true);
    t.checkExpect(this.mazeGame.pathVertices.size() == 0, true);
    t.checkExpect(this.mazeGame.tick == 0, true);
    this.mazeGame.onKeyEvent("d");
    t.checkExpect(this.mazeGame.seenVertices.size() > 0, true);
    t.checkExpect(this.mazeGame.pathVertices.size() > 0, true);

  }

  // tests the solveMaze method
  void testSolveMaze(Tester t) {
    initData();
    this.mazeGame.solveMaze("b");
    t.checkExpect(this.mazeGame.seenVertices.size() > 0, true);
    t.checkExpect(this.mazeGame.seenVertices.size() < mazeGame.boardX * mazeGame.boardY, true);
    t.checkExpect(this.mazeGame.pathVertices.get(0), this.mazeGame.board
        .get(this.mazeGame.board.size() - 1).get(this.mazeGame.board.get(0).size() - 1));
    t.checkExpect(this.mazeGame.pathVertices.get(this.mazeGame.pathVertices.size() - 1),
        this.mazeGame.board.get(0).get(0));

    this.mazeGame.solveMaze("d");
    t.checkExpect(this.mazeGame.seenVertices.size() > 0, true);
    t.checkExpect(this.mazeGame.seenVertices.size() < mazeGame.boardX * mazeGame.boardY, true);
    t.checkExpect(this.mazeGame.pathVertices.get(0), this.mazeGame.board
        .get(this.mazeGame.board.size() - 1).get(this.mazeGame.board.get(0).size() - 1));
    t.checkExpect(this.mazeGame.pathVertices.get(this.mazeGame.pathVertices.size() - 1),
        this.mazeGame.board.get(0).get(0));

  }

  // tests the reconstruct method
  void testReconstruct(Tester t) {
    initData();
    Vertex aa = new Vertex(new Posn(0, 0), new ArrayList<Edge>());
    Vertex bb = new Vertex(new Posn(0, 1), new ArrayList<Edge>());
    Vertex cc = new Vertex(new Posn(0, 2), new ArrayList<Edge>());
    Vertex dd = new Vertex(new Posn(1, 0), new ArrayList<Edge>());
    Vertex ee = new Vertex(new Posn(1, 1), new ArrayList<Edge>());
    Vertex ff = new Vertex(new Posn(1, 2), new ArrayList<Edge>());

    ArrayList<Vertex> row1 = new ArrayList<Vertex>(Arrays.asList(aa, bb, cc));
    ArrayList<Vertex> row2 = new ArrayList<Vertex>(Arrays.asList(dd, ee, ff));

    ArrayList<ArrayList<Vertex>> board1 = new ArrayList<ArrayList<Vertex>>(
        Arrays.asList(row1, row2));

    HashMap<Vertex, Vertex> cameFromEdge = new HashMap<Vertex, Vertex>();
    cameFromEdge.put(bb, aa);
    cameFromEdge.put(cc, bb);
    cameFromEdge.put(dd, cc);
    cameFromEdge.put(ee, dd);
    cameFromEdge.put(ff, dd);

    ArrayList<Vertex> reconstruct1 = new ArrayList<Vertex>(Arrays.asList(ff, cc, bb, aa));

    this.mazeGame.board = board1;

    this.mazeGame.reconstruct(cameFromEdge, dd);
    t.checkExpect(this.mazeGame.pathVertices, reconstruct1);

  }

  // tests ontick method
  void testOnTick(Tester t) {
    Vertex aa = new Vertex(new Posn(0, 0), new ArrayList<Edge>());
    Vertex bb = new Vertex(new Posn(0, 1), new ArrayList<Edge>());
    Vertex cc = new Vertex(new Posn(0, 2), new ArrayList<Edge>());
    Vertex dd = new Vertex(new Posn(1, 0), new ArrayList<Edge>());
    Vertex ee = new Vertex(new Posn(1, 1), new ArrayList<Edge>());
    Vertex ff = new Vertex(new Posn(1, 2), new ArrayList<Edge>());

    ArrayList<Vertex> row1 = new ArrayList<Vertex>(Arrays.asList(aa, bb, cc));
    ArrayList<Vertex> row2 = new ArrayList<Vertex>(Arrays.asList(dd, ee, ff));

    ArrayList<ArrayList<Vertex>> board1 = new ArrayList<ArrayList<Vertex>>(
        Arrays.asList(row1, row2));
    MazeGame testGame = new MazeGame(10, 10);
    testGame.board = board1;

    t.checkExpect(testGame.seenVertices.size(), 0);
    t.checkExpect(testGame.pathVertices.size(), 0);
    t.checkExpect(testGame.tick, 0);

    testGame.onTick();
    t.checkExpect(testGame.seenVertices.size(), 0);
    t.checkExpect(testGame.pathVertices.size(), 0);
    t.checkExpect(testGame.tick, 1);

  }

  // testing the equals method in the edge class
  void testEdgeEquals(Tester t) {
    initData();

    // testing whether the same edge is equal
    t.checkExpect(this.aToB.equals(this.aToB), true);
    t.checkExpect(this.fToD.equals(this.fToD), true);
    t.checkExpect(this.eToC.equals(this.eToC), true);

    // testing whether two different edges are equal
    t.checkExpect(this.bToE.equals(this.aToE), false);
    t.checkExpect(this.aToE.equals(this.bToE), false);
    t.checkExpect(this.cToD.equals(this.fToD), false);
    t.checkExpect(this.fToD.equals(this.cToD), false);

    // testing whether an edge is equal to a null value
    t.checkExpect(this.eToC == null, false);
    t.checkExpect(this.bToE == null, false);
    t.checkExpect(this.aToB == null, false);

    // testing whether two edges of the same weight are equal
    t.checkExpect(this.aToE.equals(this.bToF), false);
    t.checkExpect(this.bToF.equals(this.aToE), false);
    t.checkExpect(this.fToD.equals(this.bToF), false);
    t.checkExpect(this.fToD.equals(this.aToE), false);

    // testing whether two edges with the same vertextes but different weights are
    // equal
    t.checkExpect(this.fToD.equals(new Edge(this.f, this.d, 30)), false);
    t.checkExpect(this.bToF.equals(new Edge(this.b, this.f, 100)), false);
    t.checkExpect(this.cToD.equals(new Edge(this.c, this.d, 50)), false);

    // testing whether two edges with flipped from and to vertextes are equal
    t.checkExpect(this.bToF.equals(new Edge(this.f, this.b, 50)), true);
    t.checkExpect(this.bToC.equals(new Edge(this.c, this.b, 40)), true);
    t.checkExpect(this.eToC.equals(new Edge(this.c, this.e, 15)), true);

    // testing whether two edges with flipped from and to vertextes and different
    // weights are equal
    t.checkExpect(this.bToF.equals(new Edge(this.f, this.b, 10)), false);
    t.checkExpect(this.bToC.equals(new Edge(this.c, this.b, 15)), false);
    t.checkExpect(this.eToC.equals(new Edge(this.c, this.e, 20)), false);

  }

  // testing the hashCode in the edge class
  void testEdgeHashCode(Tester t) {
    initData();

    // testing the hashcode of each edge
    t.checkExpect(this.aToB.hashCode(), 300000);
    t.checkExpect(this.aToE.hashCode(), 1000000);
    t.checkExpect(this.bToF.hashCode(), 2000000);
    t.checkExpect(this.bToC.hashCode(), 1200000);
    t.checkExpect(this.bToE.hashCode(), 1050000);
    t.checkExpect(this.eToC.hashCode(), 600000);
    t.checkExpect(this.cToD.hashCode(), 750000);
    t.checkExpect(this.fToD.hashCode(), 2000000);

    // testing to make sure the hashcodes are different
    t.checkExpect(this.aToB.hashCode() == this.aToE.hashCode(), false);
    t.checkExpect(this.aToE.hashCode() == this.aToB.hashCode(), false);
    t.checkExpect(this.bToF.hashCode() == this.bToC.hashCode(), false);
    t.checkExpect(this.bToC.hashCode() == this.bToF.hashCode(), false);
    t.checkExpect(this.bToE.hashCode() == this.eToC.hashCode(), false);
    t.checkExpect(this.eToC.hashCode() == this.bToE.hashCode(), false);
    t.checkExpect(this.cToD.hashCode() == this.fToD.hashCode(), false);
    t.checkExpect(this.fToD.hashCode() == this.cToD.hashCode(), false);

    // testing the hashcodes for two equal edges
    t.checkExpect(this.aToB.hashCode(), (new Edge(this.a, this.b, 30).hashCode()));
    t.checkExpect(this.aToE.hashCode(), (new Edge(this.a, this.e, 50).hashCode()));
    t.checkExpect(this.bToF.hashCode(), (new Edge(this.b, this.f, 50).hashCode()));
    t.checkExpect(this.bToC.hashCode(), (new Edge(this.b, this.c, 40).hashCode()));
    t.checkExpect(this.bToE.hashCode(), (new Edge(this.b, this.e, 35).hashCode()));
    t.checkExpect(this.eToC.hashCode(), (new Edge(this.e, this.c, 15).hashCode()));
    t.checkExpect(this.cToD.hashCode(), (new Edge(this.c, this.d, 25).hashCode()));
    t.checkExpect(this.fToD.hashCode(), (new Edge(this.f, this.d, 50).hashCode()));

    // testing the hashcodes for when the to and from vertexes are flipped
    t.checkExpect(this.aToB.hashCode(), (new Edge(this.b, this.a, 30)).hashCode());
    t.checkExpect(this.aToE.hashCode(), (new Edge(this.e, this.a, 50)).hashCode());
    t.checkExpect(this.bToF.hashCode(), (new Edge(this.f, this.b, 50)).hashCode());
    t.checkExpect(this.bToC.hashCode(), (new Edge(this.c, this.b, 40)).hashCode());
    t.checkExpect(this.bToE.hashCode(), (new Edge(this.e, this.b, 35)).hashCode());
    t.checkExpect(this.eToC.hashCode(), (new Edge(this.c, this.e, 15)).hashCode());
    t.checkExpect(this.cToD.hashCode(), (new Edge(this.d, this.c, 25)).hashCode());
    t.checkExpect(this.fToD.hashCode(), (new Edge(this.d, this.f, 50)).hashCode());

    // testing the hashcodes for when the weight is different
    t.checkExpect(this.fToD.hashCode() == (new Edge(this.f, this.d, 0)).hashCode(), false);
    t.checkExpect(this.cToD.hashCode() == (new Edge(this.c, this.d, 10)).hashCode(), false);
    t.checkExpect(this.eToC.hashCode() == (new Edge(this.e, this.c, 20)).hashCode(), false);

    // testing the hashcodes for when the vertexes are flipped and the weight is
    // different
    t.checkExpect(this.fToD.hashCode() == (new Edge(this.d, this.f, 0)).hashCode(), false);
    t.checkExpect(this.cToD.hashCode() == (new Edge(this.d, this.c, 10)).hashCode(), false);
    t.checkExpect(this.eToC.hashCode() == (new Edge(this.c, this.e, 20)).hashCode(), false);

  }

  // testing the equals method in the vertex class
  void testVertexEquals(Tester t) {
    initData();

    // testing to make sure the same vertexes are equal
    t.checkExpect(this.a.equals(this.a), true);
    t.checkExpect(this.b.equals(this.b), true);
    t.checkExpect(this.c.equals(this.c), true);
    t.checkExpect(this.d.equals(this.d), true);
    t.checkExpect(this.e.equals(this.e), true);
    t.checkExpect(this.f.equals(this.f), true);

    // testing whether different vertextes are equal
    t.checkExpect(this.a.equals(this.b), false);
    t.checkExpect(this.c.equals(this.d), false);
    t.checkExpect(this.e.equals(this.f), false);
    t.checkExpect(this.b.equals(this.a), false);
    t.checkExpect(this.d.equals(this.c), false);
    t.checkExpect(this.f.equals(this.e), false);

    // testing whether vertexes with the same posns are equal
    t.checkExpect(this.a.equals(new Vertex(new Posn(0, 0), new ArrayList<Edge>())), true);
    t.checkExpect(this.b.equals(new Vertex(new Posn(0, 1), new ArrayList<Edge>())), true);
    t.checkExpect(this.c.equals(new Vertex(new Posn(0, 2), new ArrayList<Edge>())), true);
    t.checkExpect(this.d.equals(new Vertex(new Posn(1, 0), new ArrayList<Edge>())), true);
    t.checkExpect(this.e.equals(new Vertex(new Posn(1, 1), new ArrayList<Edge>())), true);
    t.checkExpect(this.f.equals(new Vertex(new Posn(1, 2), new ArrayList<Edge>())), true);

    // testing whether vertexes with flipped posns are equal
    t.checkExpect(this.b.equals(new Vertex(new Posn(1, 0), new ArrayList<Edge>())), false);
    t.checkExpect(this.c.equals(new Vertex(new Posn(2, 0), new ArrayList<Edge>())), false);
    t.checkExpect(this.d.equals(new Vertex(new Posn(0, 1), new ArrayList<Edge>())), false);
    t.checkExpect(this.f.equals(new Vertex(new Posn(2, 1), new ArrayList<Edge>())), false);

    // testing whether a vertex is equal to null
    t.checkExpect(this.a == null, false);
    t.checkExpect(this.c == null, false);
    t.checkExpect(this.e == null, false);
    t.checkExpect(this.b == null, false);
    t.checkExpect(this.d == null, false);
    t.checkExpect(this.f == null, false);

    // testing whether vertexes with different arraylists are equal

    // testing for a
    t.checkExpect(this.a.equals(new Vertex(new Posn(0, 0), new ArrayList<Edge>())), true);
    this.a.outer.add(aToE);
    t.checkExpect(this.a.equals(new Vertex(new Posn(0, 0), new ArrayList<Edge>())), false);

    // testing for b
    t.checkExpect(this.b.equals(new Vertex(new Posn(0, 1), new ArrayList<Edge>())), true);
    this.b.outer.add(bToE);
    t.checkExpect(this.b.equals(new Vertex(new Posn(0, 1), new ArrayList<Edge>())), false);

    // testing for c
    t.checkExpect(this.c.equals(new Vertex(new Posn(0, 2), new ArrayList<Edge>())), true);
    this.c.outer.add(eToC);
    t.checkExpect(this.c.equals(new Vertex(new Posn(0, 2), new ArrayList<Edge>())), false);

    // testing for d
    t.checkExpect(this.d.equals(new Vertex(new Posn(1, 0), new ArrayList<Edge>())), true);
    this.d.outer.add(bToC);
    t.checkExpect(this.d.equals(new Vertex(new Posn(1, 0), new ArrayList<Edge>())), false);

    // testing for e
    t.checkExpect(this.e.equals(new Vertex(new Posn(1, 1), new ArrayList<Edge>())), true);
    this.e.outer.add(bToF);
    t.checkExpect(this.e.equals(new Vertex(new Posn(1, 1), new ArrayList<Edge>())), false);

    // testing for f
    t.checkExpect(this.f.equals(new Vertex(new Posn(1, 2), new ArrayList<Edge>())), true);
    this.f.outer.add(cToD);
    t.checkExpect(this.f.equals(new Vertex(new Posn(1, 2), new ArrayList<Edge>())), false);

  }

  // testing the hashCode in the vertex class
  void testVertexHashCode(Tester t) {
    initData();

    // testing the value of the hashCodes
    t.checkExpect(this.a.hashCode(), -1);
    t.checkExpect(this.b.hashCode(), -10001);
    t.checkExpect(this.c.hashCode(), -20001);
    t.checkExpect(this.d.hashCode(), 49999);
    t.checkExpect(this.e.hashCode(), 39999);
    t.checkExpect(this.f.hashCode(), 29999);

    // testing the hashCodes of two equal values

    t.checkExpect(
        this.a.hashCode() == (new Vertex(new Posn(0, 0), new ArrayList<Edge>()).hashCode()), true);
    t.checkExpect(
        this.b.hashCode() == (new Vertex(new Posn(1, 0), new ArrayList<Edge>()).hashCode()), false);
    t.checkExpect(
        this.c.hashCode() == (new Vertex(new Posn(2, 0), new ArrayList<Edge>()).hashCode()), false);
    t.checkExpect(
        this.d.hashCode() == (new Vertex(new Posn(0, 1), new ArrayList<Edge>()).hashCode()), false);
    t.checkExpect(
        this.e.hashCode() == (new Vertex(new Posn(1, 1), new ArrayList<Edge>()).hashCode()), true);
    t.checkExpect(
        this.f.hashCode() == (new Vertex(new Posn(2, 1), new ArrayList<Edge>()).hashCode()), false);

    // testing the hashCode when the posns are switched
    t.checkExpect(this.b.hashCode(),
        (new Vertex(new Posn(0, 1), new ArrayList<Edge>()).hashCode()));
    t.checkExpect(this.c.hashCode(),
        (new Vertex(new Posn(0, 2), new ArrayList<Edge>()).hashCode()));
    t.checkExpect(this.d.hashCode(),
        (new Vertex(new Posn(1, 0), new ArrayList<Edge>()).hashCode()));
    t.checkExpect(this.f.hashCode(),
        (new Vertex(new Posn(1, 2), new ArrayList<Edge>()).hashCode()));

    // testing the hashCode when the arrayLists are different

    // testing for a
    t.checkExpect(
        this.a.hashCode() == (new Vertex(new Posn(0, 0), new ArrayList<Edge>())).hashCode(), true);
    this.a.outer.add(aToE);
    t.checkExpect(
        this.a.hashCode() == (new Vertex(new Posn(0, 0), new ArrayList<Edge>())).hashCode(), false);

    // testing for b
    t.checkExpect(
        this.b.hashCode() == (new Vertex(new Posn(0, 1), new ArrayList<Edge>())).hashCode(), true);
    this.b.outer.add(bToE);
    t.checkExpect(
        this.b.hashCode() == (new Vertex(new Posn(0, 1), new ArrayList<Edge>())).hashCode(), false);

    // testing for c
    t.checkExpect(
        this.c.hashCode() == (new Vertex(new Posn(0, 2), new ArrayList<Edge>())).hashCode(), true);
    this.c.outer.add(eToC);
    t.checkExpect(
        this.c.hashCode() == (new Vertex(new Posn(0, 2), new ArrayList<Edge>())).hashCode(), false);

    // testing for d
    t.checkExpect(
        this.d.hashCode() == (new Vertex(new Posn(1, 0), new ArrayList<Edge>())).hashCode(), true);
    this.d.outer.add(bToC);
    t.checkExpect(
        this.d.hashCode() == (new Vertex(new Posn(1, 0), new ArrayList<Edge>())).hashCode(), false);

    // testing for e
    t.checkExpect(
        this.e.hashCode() == (new Vertex(new Posn(1, 1), new ArrayList<Edge>())).hashCode(), true);
    this.e.outer.add(bToF);
    t.checkExpect(
        this.e.hashCode() == (new Vertex(new Posn(1, 1), new ArrayList<Edge>())).hashCode(), false);

    // testing for f
    t.checkExpect(
        this.f.hashCode() == (new Vertex(new Posn(1, 2), new ArrayList<Edge>())).hashCode(), true);
    this.f.outer.add(cToD);
    t.checkExpect(
        this.f.hashCode() == (new Vertex(new Posn(1, 2), new ArrayList<Edge>())).hashCode(), false);

  }

  // tests the drawEdge method
  void testDrawEdge(Tester t) {
    initData();

    // drawing all example edges
    t.checkExpect(this.aToB.drawEdge(), new RectangleImage(9, 9, OutlineMode.SOLID, Color.gray));
    t.checkExpect(this.aToE.drawEdge(), new RectangleImage(9, 9, OutlineMode.SOLID, Color.gray));
    t.checkExpect(this.bToF.drawEdge(), new RectangleImage(9, 9, OutlineMode.SOLID, Color.gray));
    t.checkExpect(this.bToC.drawEdge(), new RectangleImage(9, 9, OutlineMode.SOLID, Color.gray));
    t.checkExpect(this.bToE.drawEdge(), new RectangleImage(9, 9, OutlineMode.SOLID, Color.gray));
    t.checkExpect(this.eToC.drawEdge(), new RectangleImage(9, 9, OutlineMode.SOLID, Color.gray));
    t.checkExpect(this.cToD.drawEdge(), new RectangleImage(9, 9, OutlineMode.SOLID, Color.gray));
    t.checkExpect(this.fToD.drawEdge(), new RectangleImage(9, 9, OutlineMode.SOLID, Color.gray));
  }

  // tests the compare method
  void testCompare(Tester t) {
    initData();

    // testing to make sure compare returns 0 against the same values
    t.checkExpect(this.comp.compare(aToB, aToB), 0);
    t.checkExpect(this.comp.compare(aToE, aToE), 0);
    t.checkExpect(this.comp.compare(bToF, bToF), 0);
    t.checkExpect(this.comp.compare(bToC, bToC), 0);
    t.checkExpect(this.comp.compare(bToE, bToE), 0);
    t.checkExpect(this.comp.compare(eToC, eToC), 0);
    t.checkExpect(this.comp.compare(cToD, cToD), 0);
    t.checkExpect(this.comp.compare(fToD, fToD), 0);
    t.checkExpect(this.comp.compare(fToD, aToE), 0);
    t.checkExpect(this.comp.compare(fToD, bToF), 0);

    // testing compare against different values
    t.checkExpect(this.comp.compare(aToB, aToE), -20);
    t.checkExpect(this.comp.compare(bToF, bToC), 10);
    t.checkExpect(this.comp.compare(bToE, eToC), 20);
    t.checkExpect(this.comp.compare(cToD, fToD), -25);
  }

  // tests the drawCellVertex method
  void testDrawCellVertex(Tester t) {
    initData();

    // drawing the cell vertex for every vertex in examples
    t.checkExpect(this.a.drawCellVertex(), new RectangleImage(9, 9, OutlineMode.SOLID, Color.gray));
    t.checkExpect(this.b.drawCellVertex(), new RectangleImage(9, 9, OutlineMode.SOLID, Color.gray));
    t.checkExpect(this.c.drawCellVertex(), new RectangleImage(9, 9, OutlineMode.SOLID, Color.gray));
    t.checkExpect(this.d.drawCellVertex(), new RectangleImage(9, 9, OutlineMode.SOLID, Color.gray));
    t.checkExpect(this.e.drawCellVertex(), new RectangleImage(9, 9, OutlineMode.SOLID, Color.gray));
    t.checkExpect(this.f.drawCellVertex(), new RectangleImage(9, 9, OutlineMode.SOLID, Color.gray));
  }

  // testing the makeBoard method
  void testMakeBoard(Tester t) {
    initData();

    t.checkExpect(this.mazeGame.board.size(), 40);
    t.checkExpect(this.mazeGame.makeBoard().size(), 40);
    t.checkExpect(this.mazeGame.board.size(), this.mazeGame.makeBoard().size());
  }

  // testing the edgeSort method
  void testEdgeSort(Tester t) {
    initData();

    t.checkExpect(this.listEdge.size(), 8);
    t.checkExpect(this.listEdge.indexOf(this.aToB), 0);
    t.checkExpect(this.listEdge.indexOf(this.aToE), 1);
    t.checkExpect(this.listEdge.indexOf(this.bToF), 2);
    t.checkExpect(this.listEdge.indexOf(this.bToC), 3);
    t.checkExpect(this.listEdge.indexOf(this.bToE), 4);
    t.checkExpect(this.listEdge.indexOf(this.eToC), 5);
    t.checkExpect(this.listEdge.indexOf(this.cToD), 6);
    t.checkExpect(this.listEdge.indexOf(this.fToD), 7);

    this.mazeGame.edgeSort(listEdge);

    t.checkExpect(this.listEdge.size(), 8);
    t.checkExpect(this.listEdge.indexOf(this.aToB), 2);
    t.checkExpect(this.listEdge.indexOf(this.aToE), 5);
    t.checkExpect(this.listEdge.indexOf(this.bToF), 6);
    t.checkExpect(this.listEdge.indexOf(this.bToC), 4);
    t.checkExpect(this.listEdge.indexOf(this.bToE), 3);
    t.checkExpect(this.listEdge.indexOf(this.eToC), 0);
    t.checkExpect(this.listEdge.indexOf(this.cToD), 1);
    t.checkExpect(this.listEdge.indexOf(this.fToD), 7);

  }

  // testing the union method
  void testUnion(Tester t) {
    initData();

    // starting with a blank hashMap
    t.checkExpect(this.hashMap.isEmpty(), true);

    // using union
    mazeGame.union(this.hashMap, a, b);
    t.checkExpect(this.hashMap.isEmpty(), false);
    t.checkExpect(this.hashMap.size(), 1);
    t.checkExpect(this.hashMap.containsKey(a), true);
    t.checkExpect(this.hashMap.containsValue(b), true);
    t.checkExpect(this.hashMap.containsKey(b), false);
    t.checkExpect(this.hashMap.containsValue(a), false);
  }

  // testing the find method
  void testFind(Tester t) {
    initData();
    this.hashMap.put(this.a, this.e);
    this.hashMap.put(this.b, this.a);
    this.hashMap.put(this.c, this.e);
    this.hashMap.put(this.d, this.e);
    this.hashMap.put(this.e, this.e);
    this.hashMap.put(this.f, this.d);

    t.checkExpect(this.mazeGame.find(hashMap, this.a), this.e);
    t.checkExpect(this.mazeGame.find(hashMap, this.b), this.e);
    t.checkExpect(this.mazeGame.find(hashMap, this.c), this.e);
    t.checkExpect(this.mazeGame.find(hashMap, this.d), this.e);
    t.checkExpect(this.mazeGame.find(hashMap, this.e), this.e);

  }

  // tests the minimumSpanningTree method
  void testMinimumSpanningTree(Tester t) {
    initData();

    t.checkExpect(this.mazeGame.edgesInTree.size(), 1599);

    t.checkExpect(mazeGame.minimumSpanningTree(mazeGame.worklist, mazeGame.board),
        mazeGame.edgesInTree);

    MazeGame mazeGame2 = new MazeGame(40, 40);

    // testings to see if one instance of the maze that goes through the mst method
    // has a smaller worklist size than one before the mst method
    this.mazeGame.minimumSpanningTree(mazeGame.worklist, mazeGame.board);

    while (this.mazeGame.edgesInTree.size() < this.hashMap.size() - 1) {
      t.checkExpect(this.mazeGame.worklist.size() > mazeGame2.worklist.size(), false);
    }

  }

  // tests the connect edges method
  void testConnectEdges(Tester t) {
    initData();
    this.mazeGame.connectEdges();
    for (Edge test : this.mazeGame.edgesInTree) {
      t.checkExpect(test.from.outer.contains(test), true);
      t.checkExpect(test.from.outer.contains(test), true);
    }

  }
  
  // tests the allEdges method

  void testAllEdges(Tester t) {
    initData();

    MazeGame test = new MazeGame(40, 40);
    ArrayList<Edge> answer = new ArrayList<Edge>();

    for (int i = 0; i < test.board.size(); i++) {
      for (int j = 0; j < test.board.get(i).size(); j++) {

        if (i < test.boardX - 1) {
          Vertex vertex1 = test.board.get(i).get(j);
          Vertex right = test.board.get(i + 1).get(j);
          Edge edgeR = new Edge(vertex1, right, new Random().nextInt(10000));
          answer.add(edgeR);
        }

        if (j < test.boardY - 1) {
          Vertex vertex1 = test.board.get(i).get(j);
          Vertex bottom = test.board.get(i).get(j + 1);
          Edge edgeB = new Edge(vertex1, bottom, new Random().nextInt(10000));
          answer.add(edgeB);
        }
      }
    }
    t.checkExpect(this.mazeGame.allEdges().size(), answer.size());
    t.checkExpect(test.allEdges().size(), answer.size());

  }

  // tests makeScene method
  void testMakeScene(Tester t) {
    initData();

    WorldScene scene = new WorldScene(1500, 900);

    WorldImage title = new TextImage("Maze Solver", 75, FontStyle.BOLD_ITALIC, Color.GREEN);
    scene.placeImageXY(title, 800, 650);

    WorldImage directionD1 = new TextImage("Press 'D' to solve the maze", 20, FontStyle.REGULAR,
        Color.BLACK);
    WorldImage directionD2 = new TextImage("via depth-first search", 20, FontStyle.REGULAR,
        Color.BLACK);

    scene.placeImageXY(directionD1, 800, 700);
    scene.placeImageXY(directionD2, 800, 718);

    WorldImage directionS1 = new TextImage("Press 'B' to solve the maze", 20, FontStyle.REGULAR,
        Color.BLACK);
    WorldImage directionS2 = new TextImage("via breadth-first search", 20, FontStyle.REGULAR,
        Color.BLACK);

    scene.placeImageXY(directionS1, 800, 737);
    scene.placeImageXY(directionS2, 800, 755);

    WorldImage directionRestart = new TextImage("Press 'R' to restart the maze", 20,
        FontStyle.REGULAR, Color.BLACK);
    scene.placeImageXY(directionRestart, 800, 823);

    WorldImage timer = new TextImage(
        "Timer: " + Integer.toString((int) this.mazeGame.tick / 20) + " seconds", 20, Color.BLACK);
    WorldImage finalTimer = new OverlayImage(timer,
        new RectangleImage(200, 40, OutlineMode.SOLID, Color.PINK));

    scene.placeImageXY(finalTimer, 1100, 730);

    WorldImage dCount = new TextImage("DFS Count: " + Integer.toString(this.mazeGame.dCount), 20,
        Color.PINK);
    scene.placeImageXY(dCount, 500, 730);

    WorldImage bCount = new TextImage("BFS Count: " + Integer.toString(this.mazeGame.bCount), 20,
        Color.PINK);
    scene.placeImageXY(bCount, 500, 700);

    WorldImage pCount = new TextImage(
        "Solution Path Count: " + Integer.toString(this.mazeGame.pathVertices.size()), 20,
        Color.PINK);
    scene.placeImageXY(pCount, 360, 670);

    WorldImage wDCount = new TextImage(
        "Wrong DFS Moves: " + Integer.toString(this.mazeGame.wDCount), 20, Color.PINK);
    scene.placeImageXY(wDCount, 200, 730);

    WorldImage wBCount = new TextImage(
        "Wrong BFS Moves: " + Integer.toString(this.mazeGame.wBCount), 20, Color.PINK);
    scene.placeImageXY(wBCount, 200, 700);

    // draws the grid
    for (ArrayList<Vertex> list : this.mazeGame.board) {
      for (Vertex vertex : list) {
        scene.placeImageXY(vertex.drawCellVertex(), vertex.location.x * 10 + 5,
            vertex.location.y * 10 + 5);
      }
    }
    // draws the edges
    for (Edge edge : this.mazeGame.edgesInTree) {
      scene.placeImageXY(edge.drawEdge(), (edge.to.location.x + edge.from.location.x) * 5 + 5,
          (edge.to.location.y + edge.from.location.y) * 5 + 5);
    }

    WorldImage startingBlock = new RectangleImage(9, 9, OutlineMode.SOLID, Color.GREEN);
    scene.placeImageXY(startingBlock, 5, 5);

    WorldImage endingBlock = new RectangleImage(9, 9, OutlineMode.SOLID, new Color(147, 112, 219));
    scene.placeImageXY(endingBlock, this.mazeGame.boardX * 10 - 5, this.mazeGame.boardY * 10 - 5);

    t.checkExpect(this.mazeGame.makeScene(), scene);
  }
}
