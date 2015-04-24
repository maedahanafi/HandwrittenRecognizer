package recognizergui;
import java.util.ArrayList;


/**
 *
 * Maeda Hanafi
 */
public class FeedforwardNetwork {
    private ArrayList<Layer> networkLayers;

    //used to gather information from mainApp about network info
    private ArrayList<Integer> neuronCountLayer;
    private double error;
    private double globalError=0;
    private int setSize=0;

    public FeedforwardNetwork(){
        networkLayers = new ArrayList<Layer>();
        neuronCountLayer = new ArrayList<Integer>();
    }

    //simply gathers information on network structure
    public void addLayer(int neurons){
        neuronCountLayer.add(neurons);
    }

    //must be called after adding the layers in order for the network to be created
    public void create(){
        int i;
        for(i=0; i<neuronCountLayer.size()-1; i++){
            networkLayers.add(new Layer(neuronCountLayer.get(i), this, i, false));
        }
        networkLayers.add(new Layer(neuronCountLayer.get(i), this, i, true));
    }

    //used by the Layer object for getting the next layer
    public Layer getNextLayer(int currentLayer){
        if(currentLayer>=networkLayers.size()-1){
            return null;
        }
        return networkLayers.get(currentLayer+1);
    }

    //must be called after the network is created. this randomizes all the
    //weights by calling each layer's random function,
    public void randomize(){
        //dont randomize for the last layer
        for(int i=0; i<networkLayers.size()-1; i++){
            networkLayers.get(i).initWeights();
        }
    }

    public void epoch(double[][] input, double[][] goalOutput){
         //an epoch backpropagation training
        for(int j=0; j<input.length; j++){
            //input first set of input
            //Calculate Input set at [j]
            double[] output = input(input[j]);
            //for(int i=0; i<output.length; i++){
            //    System.out.println(output[i]+" ");
            //}
            calcError(j, goalOutput[j]);
        }
        //learn
        learn();
        error = calculateError(input, goalOutput);
        //System.out.println("error: "+error);
    }

    //this is finally called to determine the output of the network given a set of inputs
    public double[] input(double inputSet[]){
        for(int i=0; i<networkLayers.size()-1; i++){ //go thru each network layer except for the last one
            if (networkLayers.get(i).isInputLayer()) {
                networkLayers.get(i).computeOutputs(inputSet);
            } else if (networkLayers.get(i).isHiddenLayer()) {

                networkLayers.get(i).computeOutputs(null);
            }
        }
        return networkLayers.get(networkLayers.size()-1).getOutput();
    }

    public void calcError(int layerIndex, double[] idealOutput){
        //clear all previous error data
        for(int i=0; i<networkLayers.size(); i++){
            networkLayers.get(i).clearError();
        }

        for(int i=networkLayers.size()-1; i>=0; i--){
            if(networkLayers.get(i).isOutputLayer()){
                //System.out.println("Calculating error for output layer:"+i);
                networkLayers.get(i).calcError(idealOutput);
            }else{
                //System.out.println("Calculating error for other layers:"+i);
                networkLayers.get(i).calcError();
            }
        }
    }

    public void learn(){
        //learn except for the last layer
        for(int i=0; i<networkLayers.size()-1; i++){
            //learning rate and momentum
            networkLayers.get(i).learn(.05,0);
        }

    }

    public double calculateError(final double input[][], final double ideal[][]){
        for (int i = 0; i < ideal.length; i++) {
            input(input[i]);
            updateError(networkLayers.get(networkLayers.size()-1).getOutput(), ideal[i]);
        }
        double err = Math.sqrt(this.globalError / (this.setSize));
	return err;
    }

    public double getError() {
        return this.error;
    }

    public void updateError(final double actual[], final double ideal[]) {
        for (int i = 0; i < actual.length; i++) {
                final double delta = ideal[i] - actual[i];
                this.globalError += delta * delta;
        }
        this.setSize += ideal.length;
    }

}
