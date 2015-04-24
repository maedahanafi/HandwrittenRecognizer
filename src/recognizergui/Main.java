package recognizergui;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;



/**
 * Maeda Hanafi
 */
public class Main extends JFrame implements Runnable {

	class SymAction implements java.awt.event.ActionListener {
            public void actionPerformed(final java.awt.event.ActionEvent event) {
                final Object object = event.getSource();
                if (object == Main.this.downSample) {
                        downSample_actionPerformed(event);
                } else if (object == Main.this.clear) {
                        clear_actionPerformed(event);
                } else if (object == Main.this.add) {
                        add_actionPerformed(event);
                } else if (object == Main.this.del) {
                        del_actionPerformed(event);
                } else if (object == Main.this.load) {
                        load_actionPerformed(event);
                } else if (object == Main.this.save) {
                        save_actionPerformed(event);
                } else if (object == Main.this.unfiltered) {
                        unfiltered_actionPerformed(event);
                } else if (object == Main.this.recognize) {
                        recognize_actionPerformed(event);
                }else if(object == Main.this.filtered){
                        findSimilar_actionPerformed(event);
                }
            }
	}

	class SymListSelection implements javax.swing.event.ListSelectionListener {
            public void valueChanged(
                final javax.swing.event.ListSelectionEvent event) {
                    final Object object = event.getSource();
                    if (object == Main.this.letters) {
                            letters_valueChanged(event);
                    }
            }
	}

	public class UpdateStats implements Runnable {
            long tries;
            double lastError;

            public void run() {
                Main.this.tries.setText("" + this.tries);
                Main.this.lastError.setText("" + this.lastError);
            }
	}

       
	static final int DOWNSAMPLE_WIDTH = 6;
	static final int DOWNSAMPLE_HEIGHT = 7;
	static final double MAX_ERROR = 0.01;

	public static void main(final String args[]) {
		(new Main()).setVisible(true);
	}

        double idealOutput[][];
	boolean halt = false;

	Entry entry;
	Sample sample;
	DefaultListModel letterListModel = new DefaultListModel();
        DefaultListModel filteredList = new DefaultListModel();
        boolean filteredUse = false;
	FeedforwardNetwork net;
      	Thread trainThread = null;

	// DECLARE_CONTROLS
	javax.swing.JLabel JLabel1 = new javax.swing.JLabel();
	javax.swing.JLabel JLabel2 = new javax.swing.JLabel();
	javax.swing.JButton downSample = new javax.swing.JButton();
	javax.swing.JButton add = new javax.swing.JButton();
	javax.swing.JButton clear = new javax.swing.JButton();
        javax.swing.JButton del = new javax.swing.JButton();
	javax.swing.JButton recognize = new javax.swing.JButton();
	javax.swing.JScrollPane JScrollPane1 = new javax.swing.JScrollPane();
	javax.swing.JList letters = new javax.swing.JList();
	javax.swing.JButton load = new javax.swing.JButton();
	javax.swing.JButton save = new javax.swing.JButton();
	javax.swing.JButton unfiltered = new javax.swing.JButton();
        javax.swing.JButton filtered = new javax.swing.JButton();
	javax.swing.JLabel JLabel3 = new javax.swing.JLabel();
	javax.swing.JLabel JLabel4 = new javax.swing.JLabel();
	javax.swing.JLabel tries = new javax.swing.JLabel();
	javax.swing.JLabel lastError = new javax.swing.JLabel();
	javax.swing.JLabel JLabel8 = new javax.swing.JLabel();
	javax.swing.JLabel JLabel5 = new javax.swing.JLabel();

	
	public Main() {
                this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().setLayout(null);
		this.entry = new Entry();
		this.entry.setLocation(168, 25);
		this.entry.setSize(200, 128);
		getContentPane().add(this.entry);

		this.sample = new Sample(DOWNSAMPLE_WIDTH, DOWNSAMPLE_HEIGHT);
		this.sample.setLocation(307, 240);
		this.sample.setSize(65, 70);

		this.entry.setSample(this.sample);
		getContentPane().add(this.sample);

		//INIT_CONTROLS
		setTitle("Java Neural Network");
		getContentPane().setLayout(null);
		setSize(405, 382);
		setVisible(false);
		this.JLabel1.setText("Letters Known");
		getContentPane().add(this.JLabel1);
		this.JLabel1.setBounds(12, 12, 84, 12);
		this.JLabel2.setText("Tries:");
		getContentPane().add(this.JLabel2);
		this.JLabel2.setBounds(12, 288, 72, 24);
		this.downSample.setText("Down Sample");
		this.downSample.setActionCommand("Down Sample");
		getContentPane().add(this.downSample);
		this.downSample.setBounds(252, 180, 120, 24);
                this.add.setText("Add");
		this.add.setActionCommand("Add");
		getContentPane().add(this.add);
		this.add.setBounds(168, 156, 84, 24);
		this.clear.setText("Clear");
		this.clear.setActionCommand("Clear");
		getContentPane().add(this.clear);
		this.clear.setBounds(168, 180, 84, 24);
		this.recognize.setText("Recognize");
		this.recognize.setActionCommand("Recognize");
		getContentPane().add(this.recognize);
		this.recognize.setBounds(252, 156, 120, 24);
		this.JScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		this.JScrollPane1.setOpaque(true);
		getContentPane().add(this.JScrollPane1);
		this.JScrollPane1.setBounds(12, 24, 144, 132);
		this.JScrollPane1.getViewport().add(this.letters);
		this.letters.setBounds(0, 0, 126, 129);
		this.del.setText("Delete");
		this.del.setActionCommand("Delete");
		getContentPane().add(this.del);
		this.del.setBounds(12, 156, 144, 24);
		this.load.setText("Load");
		this.load.setActionCommand("Load");
		getContentPane().add(this.load);
		this.load.setBounds(12, 180, 72, 24);
		this.save.setText("Save");
		this.save.setActionCommand("Save");
		getContentPane().add(this.save);
		this.save.setBounds(84, 180, 72, 24);
		this.unfiltered.setText("Unfiltered");
		this.unfiltered.setActionCommand("Begin Training");
		getContentPane().add(this.unfiltered);
		this.unfiltered.setBounds(12, 204, 144, 24);
                this.filtered.setText("Filtered");
                this.filtered.setActionCommand("Find Similar");
                getContentPane().add(this.filtered);
                this.filtered.setBounds(12, 228, 144, 24);
		this.JLabel3.setText("Last Error:");
		getContentPane().add(this.JLabel3);
		this.JLabel3.setBounds(12, 312, 72, 24);
		this.tries.setText("0");
		getContentPane().add(this.tries);
		this.tries.setBounds(96, 288, 72, 24);
		this.lastError.setText("0");
		getContentPane().add(this.lastError);
		this.lastError.setBounds(96, 312, 72, 24);
		this.JLabel8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		this.JLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		this.JLabel8.setText("Training Results");
		getContentPane().add(this.JLabel8);
		this.JLabel8.setFont(new Font("Dialog", Font.BOLD, 14));
		this.JLabel8.setBounds(12, 264, 120, 24);
		this.JLabel5.setText("Draw Letters Here");
		getContentPane().add(this.JLabel5);
		this.JLabel5.setBounds(204, 12, 144, 12);

		// REGISTER_LISTENERS
		final SymAction lSymAction = new SymAction();
		this.downSample.addActionListener(lSymAction);
		this.clear.addActionListener(lSymAction);
		this.add.addActionListener(lSymAction);
		this.del.addActionListener(lSymAction);
		final SymListSelection lSymListSelection = new SymListSelection();
		this.letters.addListSelectionListener(lSymListSelection);
		this.load.addActionListener(lSymAction);
		this.save.addActionListener(lSymAction);
		this.unfiltered.addActionListener(lSymAction);
		this.recognize.addActionListener(lSymAction);
                this.filtered.addActionListener(lSymAction);

		this.letters.setModel(this.letterListModel);

	}

	@SuppressWarnings("unchecked")
	public void add_actionPerformed(final java.awt.event.ActionEvent event) {
            int i;

            final String letter = JOptionPane.showInputDialog("Please enter a letter you would like to assign this sample to.");
            if (letter == null) {
                return;
            }
            if (letter.length() > 1 || letter.length()==0) {
                JOptionPane.showMessageDialog(this,"Please enter only a single letter.", "Error",JOptionPane.ERROR_MESSAGE);
                return;
            }
            this.entry.downSample();
            final SampleData sampleData = (SampleData) this.sample.getData().clone();
            sampleData.setLetter(letter.charAt(0));
            for (i = 0; i < this.letterListModel.size(); i++) {
                final Comparable str = (Comparable) this.letterListModel.getElementAt(i);
                SampleData strComp = (SampleData) this.letterListModel.getElementAt(i);
                if (strComp.letter == letter.charAt(0)) {
                    JOptionPane.showMessageDialog(this,"That letter is already defined, delete it first!","Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (str.compareTo(sampleData) > 0) {
                    this.letterListModel.add(i, sampleData);
                    return;
                }
            }
            this.letterListModel.add(this.letterListModel.size(), sampleData);
            this.letters.setSelectedIndex(i);
            this.entry.clear();
            this.sample.repaint();

	}

	void clear_actionPerformed(final java.awt.event.ActionEvent event) {
		this.entry.clear();
		this.sample.getData().clear();
		this.sample.repaint();
                this.letters.setModel(this.letterListModel);
                filteredUse = false;
	}

	void del_actionPerformed(final java.awt.event.ActionEvent event) {
            final int i = this.letters.getSelectedIndex();
            if (i == -1) {
                JOptionPane.showMessageDialog(this,"Please select a letter to delete.", "Error",JOptionPane.ERROR_MESSAGE);
                return;
            }
            this.letterListModel.remove(i);
	}

	void downSample_actionPerformed(final java.awt.event.ActionEvent event) {
		this.entry.downSample();
	}

        public void findSimilar_actionPerformed(final java.awt.event.ActionEvent event){
            // downsample
            this.entry.downSample();            
            //new filter list each time
            filteredList = new DefaultListModel();
            //find sample data that matches
            int j=0;
            for(int i=0; i<this.letterListModel.size(); i++){
                
                SampleData ds = (SampleData) this.letterListModel.get(i);
                 int rank = getRanks(this.entry.sample.getData().getSlopes(), ds.getSlopes()
                                    ,this.entry.sample.getData().grid, ds.grid);

                if(0<=rank){
                    filteredList.addElement(this.letterListModel.get(i));
                    SampleData ds1 = (SampleData) this.filteredList.get(j);
                    ds1.setRank(rank);
                    this.filteredList.set(j, ds1);
                    j++;
                }
            }
            //sort filteredList
            for (int k = 1; k < filteredList.size(); k++) {
                // Perform the kth pass
                for (int i = 0; i < filteredList.size() - k; i++) {
                    SampleData ds = (SampleData) this.filteredList.get(i);
                    SampleData ds1 = (SampleData) this.filteredList.get(i+1);
                    if (ds.getRank() < ds1.getRank()){
                        //swap list[i] with list[i + 1];
                        this.filteredList.set(i, ds1);
                        this.filteredList.set(i+1, ds);
                    }
                }
            }
            if(filteredList.size()>6)
                this.filteredList.removeRange(6, filteredList.size()-1);


            System.out.println("Ranking:");
            for(int i=0; i<filteredList.size(); i++){
                SampleData ds = (SampleData) this.filteredList.get(i);
                System.out.println("This letter :"+filteredList.get(i).toString()+" has ranking:"+ds.getRank());
                
            }
            //update letter box
            this.letters.setModel(this.filteredList);
            //start the training automatically
            this.filteredUse = true;
            this.trainThread = new Thread(this);
            this.trainThread.start();
        }

        public int getRanks(float input[], float known[], boolean inputG[][], boolean knownG[][]){
            int rank = 0;
            
            ArrayList<Float> uniqueKnown = new ArrayList<Float>();
            
            for(int x=0; x<known.length; x++){
                if(isUnique(uniqueKnown, known[x])){
                    uniqueKnown.add(known[x]);
                }
            }
            //for each occurence in the input grid, rank up
            int i=0;
            while(i<uniqueKnown.size()){//uniqueKnown is array of unique slopes from training grid
                for(int j=0; j<input.length; j++){
                    if(uniqueKnown.get(i)==input[j]){
                        rank++;
                    }
                    
                }
                i++;
            }
            //if pattern matches, rank up
            i=0;
            while(i<known.length ){
                for(int j=0; j<input.length; j++){
                    if(i==j && known[i]==input[j]){
                        rank++;
                    }
                }
                i++;
            }
            //pixel matching
            for(int x=0; x<knownG.length; x++){
                for(int y=0; y<knownG[0].length; y++){
                    if(knownG[x][y] == inputG[x][y])
                        rank++;
                }
            }
            return rank;
        }
        private boolean isUnique(ArrayList<Float> checkArr, float num){
            for(int i=0; i<checkArr.size(); i++)
                if(checkArr.get(i)==num)
                    return false;
            return true;
        }

	
	void letters_valueChanged(final javax.swing.event.ListSelectionEvent event) {
            if (this.letters.getSelectedIndex() == -1) {
                return;
            }
            DefaultListModel listToDisplay =null;
                if(this.filteredUse){
                    listToDisplay = this.filteredList;
                }else{
                    listToDisplay = this.letterListModel;
                }
            final SampleData selected = (SampleData) listToDisplay.getElementAt(this.letters.getSelectedIndex());
            this.sample.setData((SampleData) selected.clone());
            this.sample.repaint();
            this.entry.clear();
	}

	void load_actionPerformed(final java.awt.event.ActionEvent event) {
            try {
                FileReader f;// the actual file stream
                BufferedReader r;// used to read the file line by line

                f = new FileReader(new File("./sample.dat"));
                r = new BufferedReader(f);
                String line;
                int i = 0;

                this.letterListModel.clear();

                while ((line = r.readLine()) != null) {
                    final SampleData ds = new SampleData(line.charAt(0),
                                    Main.DOWNSAMPLE_WIDTH, Main.DOWNSAMPLE_HEIGHT);
                    this.letterListModel.add(i++, ds);
                    int idx = 2;
                    for (int y = 0; y < ds.getHeight(); y++) {
                        for (int x = 0; x < ds.getWidth(); x++) {
                            ds.setData(x, y, line.charAt(idx++) == '1');
                        }
                    }
                    ds.setSlope();
                }

                r.close();
                f.close();
                clear_actionPerformed(null);
                JOptionPane.showMessageDialog(this, "Loaded from 'sample.dat'.",
                                "Training", JOptionPane.PLAIN_MESSAGE);

            } catch (final Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + e, "Training",
                                JOptionPane.ERROR_MESSAGE);
            }


	}

	void recognize_actionPerformed(final java.awt.event.ActionEvent event) {
            if (this.net == null) {
                JOptionPane.showMessageDialog(this, "I need to be trained first!",
                                "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            this.entry.downSample();

            final double input[] = new double[this.DOWNSAMPLE_HEIGHT*this.DOWNSAMPLE_WIDTH];
            int idx = 0;
            final SampleData ds = this.sample.getData();
            for (int y = 0; y < ds.getHeight(); y++) {
                for (int x = 0; x < ds.getWidth(); x++) {
                    input[idx++] = ds.getData(x, y) ? .5 : -.5;
                }
            }

            double[] output = this.net.input(input);
            int best = -1;
            for(int j=0; j<output.length; j++){
                if(output[j]<.55+.1 && output[j]>.55-.1)
                    best = j;
            }
            if(best==-1){
               for(int j=0; j<output.length; j++){
                    if(output[j]>0 || output[j]>best)
                        best = j;
                }
            }

            if(best!=-1){
                DefaultListModel networkUsed = null;
                if(this.filteredUse){
                    networkUsed = this.filteredList;
                }else{
                    networkUsed = this.letterListModel;
                }
                SampleData ds2 = (SampleData) networkUsed.getElementAt(best);
                JOptionPane.showMessageDialog(this, "  " + ds2.letter + "   (Neuron #"
                                                + best + " fired)", "That Letter Is",
                                                JOptionPane.PLAIN_MESSAGE);
            }else{
                JOptionPane.showMessageDialog(this, "  Neurons didn't fire",
                                              "Unrecognized!",
                                              JOptionPane.PLAIN_MESSAGE);
            }

            clear_actionPerformed(null);

                
	}

	public void run() {
            if (letterListModel==null || letterListModel.size()==0) {
                JOptionPane.showMessageDialog(this,"Please add letters before training.", "Error",JOptionPane.ERROR_MESSAGE);
                return;
            }
            DefaultListModel networkInput = null;
            if(this.filteredUse){
                networkInput = this.filteredList;
            }else{
                networkInput = this.letterListModel;
            }
            
            try {
                final int inputNeuron = Main.DOWNSAMPLE_HEIGHT * Main.DOWNSAMPLE_WIDTH;
                final int outputNeuron = networkInput.size();

                final double set[][] = new double[networkInput.size()][inputNeuron];
                idealOutput = new double[outputNeuron][outputNeuron];
                for (int t = 0; t < networkInput.size(); t++) {
                    int idx = 0;
                    final SampleData ds = (SampleData) networkInput.getElementAt(t);
                    for (int y = 0; y < ds.getHeight(); y++) {
                        for (int x = 0; x < ds.getWidth(); x++) {
                            set[t][idx++] = ds.getData(x, y) ? .5 : -.5;
                            
                        }
                    }
                    for(int k=0; k<idealOutput[0].length; k++){
                        if(k==t){
                            idealOutput[t][k]=.5;
                        }else{
                            idealOutput[t][k] = -.5;
                        }
                    }
                }
                           
                net = new FeedforwardNetwork();
                net.addLayer(inputNeuron);
                net.addLayer((inputNeuron+outputNeuron)/2);
                net.addLayer(outputNeuron);
                net.create();
                net.randomize();
                
                int tries = 1;

                do {
                    net.epoch(set, idealOutput);
                    update(tries++, net.getError());
                } while ((net.getError() > MAX_ERROR) );

                this.halt = true;
                update(tries, net.getError());

            } catch (final Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + e, "Training",
                                JOptionPane.ERROR_MESSAGE);
            }

	}

       

	public void save_actionPerformed(final java.awt.event.ActionEvent event) {
            try {
                OutputStream os;// the actual file stream
                PrintStream ps;// used to read the file line by line

                os = new FileOutputStream("./sample.dat", false);
                ps = new PrintStream(os);

                for (int i = 0; i < this.letterListModel.size(); i++) {
                    final SampleData ds = (SampleData) this.letterListModel
                                    .elementAt(i);
                    ps.print(ds.getLetter() + ":");
                    for (int y = 0; y < ds.getHeight(); y++) {
                        for (int x = 0; x < ds.getWidth(); x++) {
                            ps.print(ds.getData(x, y) ? "1" : "0");
                        }
                    }
                    ps.println("");
                }

                ps.close();
                os.close();
                clear_actionPerformed(null);
                JOptionPane.showMessageDialog(this, "Saved to 'sample.dat'.",
                                "Training", JOptionPane.PLAIN_MESSAGE);

            } catch (final Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + e, "Training",
                                JOptionPane.ERROR_MESSAGE);
            }

	}

	
	void unfiltered_actionPerformed(final java.awt.event.ActionEvent event) {
            this.filteredUse = false;

            if (this.trainThread == null) {
                this.unfiltered.repaint();
                this.trainThread = new Thread(this);
                this.trainThread.start();
            } else {
                this.halt = true;
            }
	}

	public void update(final int retry, final double error) {

		if (this.halt) {
			this.trainThread = null;
			JOptionPane.showMessageDialog(this, "Training has completed.",
					"Training", JOptionPane.PLAIN_MESSAGE);
                        halt = false;
		}
		final UpdateStats stats = new UpdateStats();
		stats.tries = retry;
		stats.lastError = error;
		try {
			SwingUtilities.invokeAndWait(stats);
		} catch (final Exception e) {
			JOptionPane.showMessageDialog(this, "Error: " + e, "Training",
					JOptionPane.ERROR_MESSAGE);
		}
                
	}

}
