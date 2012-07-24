/*
 Copyright (c) 2007, Distributed Computing Group (DCG)
                    ETH Zurich
                    Switzerland
                    dcg.ethz.ch

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the
   distribution.

 - Neither the name 'Sinalgo' nor the names of its contributors may be
   used to endorse or promote products derived from this software
   without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package projects.ids_wsn;


import java.awt.Color;
import java.io.IOException;

import javax.swing.JOptionPane;

import projects.ids_wsn.nodeDefinitions.BasicNode;
import projects.ids_wsn.nodeDefinitions.chord.UtilsChord;
import projects.ids_wsn.nodes.nodeImplementations.SimpleEvent;
import sinalgo.io.positionFile.PositionFileIO;
import sinalgo.nodes.Connections;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.runtime.AbstractCustomGlobal;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;
import sinalgo.tools.storage.ReusableListIterator;

/**
 * This class holds customized global state and methods for the framework. 
 * The only mandatory method to overwrite is 
 * <code>hasTerminated</code>
 * <br>
 * Optional methods to override are
 * <ul>
 * <li><code>customPaint</code></li>
 * <li><code>handleEmptyEventQueue</code></li>
 * <li><code>onExit</code></li>
 * <li><code>preRun</code></li>
 * <li><code>preRound</code></li>
 * <li><code>postRound</code></li>
 * <li><code>checkProjectRequirements</code></li>
 * </ul>
 * @see sinalgo.runtime.AbstractCustomGlobal for more details.
 * <br>
 * In addition, this class also provides the possibility to extend the framework with
 * custom methods that can be called either through the menu or via a button that is
 * added to the GUI. 
 */
public class CustomGlobal extends AbstractCustomGlobal{
	
	/* (non-Javadoc)
	 * @see runtime.AbstractCustomGlobal#hasTerminated()
	 */
	public boolean hasTerminated() {
		return false;
	}

	/**
	 * An example of a method that will be available through the menu of the GUI.
	 */
	@AbstractCustomGlobal.GlobalMethod(menuText="Echo")
	public void echo() {
		// Query the user for an input
		String answer = JOptionPane.showInputDialog(null, "This is an example.\nType in any text to echo.");
		// Show an information message 
		JOptionPane.showMessageDialog(null, "You typed '" + answer + "'", "Example Echo", JOptionPane.INFORMATION_MESSAGE);
	}
	
	@AbstractCustomGlobal.GlobalMethod(menuText="Generate Nodes Energy Log")
	public void PrintNodesEnergy() {
		for (Node n : Tools.getNodeList()){
			if (n instanceof BasicNode){
				BasicNode node = (BasicNode)n;
				Logging log = Logging.getLogger("nodes_energy");
				String msg = node.ID+";"+node.getBateria().getEnergy()+";"+node.getBateria().getTotalSpentEnergy();
				log.logln(msg);
				System.out.println(msg);
			}
		}
		JOptionPane.showMessageDialog(null, "Process Completed", "WSN", JOptionPane.INFORMATION_MESSAGE);
	}
	
	@AbstractCustomGlobal.GlobalMethod(menuText="Print energy spent per round")
	public void PrintEnergySpentPerRound() throws IOException {
		UtilsChord.gerarLogDeEnergiaPerRound("arquivo.txt");
		JOptionPane.showMessageDialog(null, "Process Completed", "WSN", JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * An example to add a button to the user interface. In this sample, the button is labeled
	 * with a text 'GO'. Alternatively, you can specify an icon that is shown on the button. See
	 * AbstractCustomGlobal.CustomButton for more details.   
	 */
	@AbstractCustomGlobal.CustomButton(buttonText="GO", toolTipText="A sample button")
	public void sampleButton() {
		JOptionPane.showMessageDialog(null, "You Pressed the 'GO' button.");
	}
	
	@AbstractCustomGlobal.GlobalMethod(menuText="Localizar Nó")
	public void localizarNo() {
		// Query the user for an input
		String resp = JOptionPane.showInputDialog(null, "Digite o ID nó");
	    Integer idNo = Integer.valueOf(resp);
		// Show an information message 
		Node no = Tools.getNodeByID(idNo);
		if (no == null){
			JOptionPane.showInputDialog(null, "Nó não existe");
		}else{
			no.setColor(Color.RED);
		}
	}
	
	@GlobalMethod(menuText="Generate Position File")
	public void generateFilePosition() {
		PositionFileIO.printPos(null);
		JOptionPane.showMessageDialog(null, "Position File Generated");
	}
	
	@GlobalMethod(menuText="Remove Events Edges")
	public void removeBasicEventEdges() {
		for(Node n : Tools.getNodeList()) {
			
			if (n instanceof SimpleEvent){
				Connections conn = n.outgoingConnections;
				ReusableListIterator<Edge> edgeList = conn.iterator();
				edgeList.reset();
				while (edgeList.hasNext()){
					Edge e = edgeList.next();
					e.defaultColor = Color.WHITE;
				}
			}
		}
	}
}
