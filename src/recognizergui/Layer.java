
package recognizergui;


/**
 *
 * Layer.java
 */
public class Layer {
        private FeedforwardNetwork network;
        //number of neurons in the layer
        private int neuronCount;
        //next layer
        private Layer next = null;
        //matrix that holds the neurons weights and threshold
        private double[][] weightThreshold;
        //the program gets the previous layer's outputs from this array
        private double[] fire;
        //layer number
        private int layerNumber;
        //indicates if this is an output layer
        boolean outputlayer;

        //backpropagation variables
        double[] errors;
        double[] errorDelta;
        private double[][] accMatrixDelta;
	private int biasRow;
	private double[][] matrixDelta;

	public Layer(int neuronCount, FeedforwardNetwork network, int layerNumber, boolean outputlayer) {
            this.neuronCount = neuronCount;
            this.network = network;
            this.layerNumber = layerNumber;
            fire = new double[neuronCount];
            this.outputlayer = outputlayer;

            errors = new double[neuronCount];
            errorDelta = new double[neuronCount];


        }

        public void setNextLayer(Layer set){
            next = set;
        }

        public void createArr(){
            if (next != null) {
                accMatrixDelta = new double[getNeuronCount() + 1][next.getNeuronCount()];
                matrixDelta = new double[getNeuronCount() + 1][next.getNeuronCount()];
                biasRow = getNeuronCount();
            }
        }

        public void initWeights(){
            //set the next layer
            setNextLayer(network.getNextLayer(layerNumber));
            //determine the number of neurons in the next layer
            int neuronNextLayer = next.neuronCount;
            //create arrays for backpopagation learning
            createArr();
            //set the weightThreshold
            weightThreshold = new double[neuronCount+1][neuronNextLayer];
            //System.out.println("weightThreshold row:"+weightThreshold.length+" col:"+weightThreshold[0].length);
            //randomize all the elements. range is between -.5 and .5
            for(int i=0; i<weightThreshold.length; i++){
                for(int j=0; j<weightThreshold[i].length; j++){
                    weightThreshold[i][j] = getRandomNumber();
                }
            }
        }

        //generate a double between -.5 and .5
        private double getRandomNumber(){
            double rand = Math.random();//produce a number between 0 and 1
            rand = rand - 0.5;
            return rand;
        }

        public void computeOutputs(double[] input){
            //if it is not a hidden layer then fill the fire array with input
            if (input != null) {
                for (int i=0; i<fire.length; i++) {
                    fire[i] = input[i];
                }
            }

            //if this a hidden layer then we set the input to be
            double layerInput[] = new double[fire.length+ 1];
            for(int i=0; i<fire.length; i++){
                layerInput[i] = fire[i];
            }
            //add in another element for threshold to be multiplied by 1
            layerInput[fire.length] = 1;

            //compute outputs
            for (int i=0; i<next.getNeuronCount(); i++) {
                //get column at i of weightThreshold
                double colWeightThreshold[] = new double[weightThreshold.length];
                for(int j=0; j<colWeightThreshold.length; j++){
                    colWeightThreshold[j] = weightThreshold[j][i];
                }

                double sum = dotProduct(colWeightThreshold, layerInput);
                //activation function on the sum and store neuron output
                next.fire[i] = activationFunction(sum);//(exp(sum*2.0)-1.0)/(exp(sum*2.0)+1.0);
                //System.out.println("output["+i+"]: "+output[i]);

            }

        }

        //backpropagation
        public void calcError(){
            for (int i = 0; i < next.getNeuronCount(); i++) {
                for (int j = 0; j < getNeuronCount(); j++) {
                    //System.out.println("weightThreshold["+j+"][,"+i+"] rows:"+weightThreshold.length+" col:"+weightThreshold[0].length);
                    accMatrixDelta[j][i] = accMatrixDelta[j][i] + next.errorDelta[i]*fire[j];
                    errors[j] = bound(errors[j] + weightThreshold[j][i]*next.getErrorDelta(i));
                }
                accMatrixDelta[biasRow][i] = accMatrixDelta[biasRow][i] + next.getErrorDelta(i);
            }

            if (isHiddenLayer()) {
                // hidden layer deltas
                for (int i = 0; i < getNeuronCount(); i++) {
                    setErrorDelta(i, bound(calculateDelta(i)));
                }
            }
        }

        public void calcError(double[] idealOutput){
            // layer errors and deltas for output layer
            for (int i = 0; i < getNeuronCount(); i++) {
                setError(i, idealOutput[i] - fire[i]);
                setErrorDelta(i, bound(calculateDelta(i)));
            }

        }

        public void learn(double learningRate, double momentum){
            double[][] m1 = multiply(accMatrixDelta, learningRate);
            double[][] m2 = multiply(matrixDelta, momentum);
            matrixDelta = add(m1, m2);

            double[][] newWeightThreshold = add(weightThreshold, matrixDelta);
            weightThreshold = newWeightThreshold;

            //clear matrix
            for(int i=0; i<accMatrixDelta.length; i++)
                for(int j=0; j<accMatrixDelta[i].length; j++)
                    accMatrixDelta[i][j] = 0;

            /*System.out.println("NEW WEIGHT MATRIX for "+layerNumber+": ");
            for(int i=0; i<weightThreshold.length; i++){
                for(int j=0; j<weightThreshold[i].length; j++){
                    System.out.print(weightThreshold[i][j]+" ");
                }
                System.out.println();
            }*/
        }

        public double dotProduct(double[] arr1, double[] arr2){
            double result = 0;
            final int length = arr1.length;

            for (int i = 0; i < length; i++) {
                    result += arr1[i] * arr2[i];
            }

            return result;
        }

        public boolean isInputLayer(){
            if(layerNumber==0)
                return true;
            else
                return false;
        }

        public boolean isHiddenLayer(){
            if(layerNumber!=0 && next!=null)
                return true;
            else
                return false;
        }

        public double[] getOutput(){
            return fire;
        }

        public int getNeuronCount(){
            return neuronCount;
        }

        public double getErrorDelta(int index){
            return errorDelta[index];
        }
        public boolean isOutputLayer(){
            if(next==null)
                return true;
            else
                return false;
        }

        public void setError(int index, double e) {
		errors[index] = bound(e);
	}

        public void setErrorDelta(int index, double d) {
		errorDelta[index] = d;
	}

        private double calculateDelta(int i) {
		return errors[i] * derivativeFunction(fire[i]);
	}

        public double derivativeFunction(double d) {
		return( 1.0-Math.pow(activationFunction(d), 2.0) );
	}

        public double activationFunction(double d) {
		double result = (exp(d*2.0)-1.0)/(exp(d*2.0)+1.0);
		return result;
	}


        public double[][] add(double[][] a, double[][] b){
            double result[][] = new double[a.length][a[0].length];

            for (int resultRow = 0; resultRow < a.length; resultRow++) {
                for (int resultCol = 0; resultCol < a[0].length; resultCol++) {
                    result[resultRow][resultCol] = a[resultRow][resultCol] + b[resultRow][resultCol];
                }
            }

            return result;
        }
        public double[][] multiply(double[][] a, double b) {
            final double result[][] = new double[a.length][a[0].length];
            for (int row = 0; row < a.length; row++) {
                for (int col = 0; col < a[0].length; col++) {
                    result[row][col] = a[row][col] * b;
                }
            }
            return result;
	}

        // Too small of a number.
	private static final double TOO_SMALL = -1.0E20;

	//Too big of a number.
	private static final double TOO_BIG = 1.0E20;

	private static double bound(final double d) {
		if (d < TOO_SMALL) {
			return TOO_SMALL;
		} else if (d > TOO_BIG) {
			return TOO_BIG;
		} else {
			return d;
		}
	}

	private static double exp(final double d) {
		return bound(Math.exp(d));
	}

        public void clearError() {
		for (int i = 0; i < getNeuronCount(); i++) {
			errors[i] = 0;
		}
	}
}
