package recognizergui;

import java.util.ArrayList;

/**
 * Maeda Hanafi
 * Class for downsampled data
 */
public class SampleData implements Comparable<SampleData>, Cloneable {

	protected boolean grid[][];
	protected char letter;
        
        //Ranking only used for letters in filtered list
        private int rank = -1;
        // slope array used for filtering
        private ArrayList<Float> slopes;
	
	public SampleData(final char letter, final int width, final int height) {
            this.grid = new boolean[width][height];
            this.letter = letter;
            this.slopes = new ArrayList<Float>();
	}

	public void clear() {
            for (int x = 0; x < this.grid.length; x++) {
                for (int y = 0; y < this.grid[0].length; y++) {
                        this.grid[x][y] = false;
                }
            }
	}

	@Override
	public Object clone(){
            final SampleData obj = new SampleData(this.letter, getWidth(),
                            getHeight());
            for (int y = 0; y < getHeight(); y++) {
                for (int x = 0; x < getWidth(); x++) {
                        obj.setData(x, y, getData(x, y));
                }
            }
            return obj;
	}

	public int compareTo(final SampleData o) {
		final SampleData obj = o;
		if (this.getLetter() > obj.getLetter()) {
			return 1;
		} else {
			return -1;
		}
	}

	
	public boolean getData(final int x, final int y) {
		return this.grid[x][y];
	}

	
	public int getHeight() {
		return this.grid[0].length;
	}

	
	public char getLetter() {
		return this.letter;
	}

	public int getWidth() {
		return this.grid.length;
	}

	
	public void setData(final int x, final int y, final boolean v) {
		this.grid[x][y] = v;
	}

	
	public void setLetter(final char letter) {
		this.letter = letter;
	}

	
	@Override
	public String toString() {
		return "" + this.letter;
	}

        public void setRank(int inRank){
            rank = inRank;
        }
        public int getRank(){
            return rank;
        }

        //Traces the image from left to right and determines characteristic
        public void setSlope(){
            slopes = new ArrayList<Float>();
            //trace grid
            for(int x=1; x<getWidth()-1; x++){
                for(int y=1; y<getHeight()-1; y++){
                    //analyze the nighbors of grid[x][y]
                    int numPoints = 0;
                    for(int j=x-1; j<=x+1; j++){
                        for(int i=y-1; i<=y+1; i++){
                            if(grid[j][i]){
                                numPoints++;
                            }
                        }
                    }
                    double[][] points = new double[numPoints][2];
                    int a = 0;
                    for(int j=x-1; j<=x+1; j++){
                        for(int i=y-1; i<=y+1; i++){
                            if(grid[j][i]){
                                points[a][0] = j;
                                points[a][1] = i;
                               
                                a++;
                            }
                        }
                    }
                    double slope = this.getSlope(points);
                    
                    if(this.slopes.size()>0){                        
                        //only accumulate the slope to array if the previous slope is not equal to new slope
                        if(Double.isNaN(slope)){
                            if(!(Double.isNaN(slopes.get(slopes.size()-1)) && Double.isNaN((slope))))
                                slopes.add((float)slope);
                        }else if(slopes.get(slopes.size()-1)!=(int)(slope*10)  ){
                                this.slopes.add((float)((int)(slope*10)));
                            }
                        
                    }else if(this.slopes.isEmpty()){
                        this.slopes.add((float)((int)(slope*10)));
                        
                    }
                }
            }
                      

        }
        // get slope of the points
        private double getSlope(double[][] points){
            Regression regress = new Regression();
            double coef[] = regress.linear_equation(points, 1);
            return coef[1];
        }
        // get arraylisy of slopes of image
        public float[] getSlopes(){
            float[] retSlopes = new float[this.slopes.size()];
            for(int i=0; i<retSlopes.length; i++){
                retSlopes[i] = slopes.get(i);
            }
            return retSlopes;
        }

}