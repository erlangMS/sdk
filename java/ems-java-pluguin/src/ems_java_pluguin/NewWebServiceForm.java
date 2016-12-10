package ems_java_pluguin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.custom.StackLayout;

public class NewWebServiceForm {
	protected Shell shell;
	//private IWorkbenchWindow window;
	private List<CampoInfo> campos = null;
	private Combo cboDatabase;
	private Combo cboTabela;
	private Database database;
	private Text txtNomeClassePojo;
	private Text txtNomeClasseDao;
	private Text txtNomeClasseNegocio;
	private Text txtNomeClasseVisao;
	private Group grpNomeObjetos;
	//private TableColumn colFiltro;
	private StyledText txtScript;
	private Text txtNomeCasoUso;
	private Text txtHomeProjeto;
	private Text txtSiglaProjeto;
	private Text txtWorkspacePojo;
	private Table grdMapeamento;
	private Text txtMaxLength;
	private Text txtCaption;
	private Text txtVar;
	private Text txtValorDefault;
	private Text txtMax;
	private Text txtMin;
	private CCombo cboObrigatorio;
	private CCombo cboPermiteNulo;
	private CCombo cboRegra;
	private CCombo cboFiltravel;
	private CCombo cboMapear;
	private CCombo cboWidget;
	private CampoInfo editaCampo;

	
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			NewWebServiceForm window = new NewWebServiceForm();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		SWTUtil.centralize(shell);
		shell.open();
		shell.layout();
		shell.forceActive();
		shell.setFocus();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(704, 635);
		shell.setText("Gerador de Web service :: Erlangms");
		shell.setLayout(new StackLayout());
		Composite composite_2 = new Composite(shell, SWT.NONE);
		TabFolder tabFolder = new TabFolder(composite_2, SWT.NONE);
		tabFolder.setBounds(0, 0, 668, 535);
		
		TabItem tbtmBancoDeDados = new TabItem(tabFolder, SWT.NONE);
		tbtmBancoDeDados.setText("Configurações Gerais");
		
		Composite composite_3 = new Composite(tabFolder, SWT.NONE);
		tbtmBancoDeDados.setControl(composite_3);
		
		cboDatabase = new Combo(composite_3, SWT.NONE);
		cboDatabase.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				e.doit = false;	
			}
		});
		cboDatabase.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selecionaDatabase();
			}
		});
		cboDatabase.setBounds(14, 31, 198, 8);
		
		Label lblNewLabel = new Label(composite_3, SWT.NONE);
		lblNewLabel.setBounds(10, 13, 130, 14);
		lblNewLabel.setText("Banco de dados:");
		
		Label lblUsurio = new Label(composite_3, SWT.NONE);
		lblUsurio.setBounds(218, 13, 55, 14);
		lblUsurio.setText("Tabela:");
		
		cboTabela = new Combo(composite_3, SWT.NONE);
		cboTabela.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				e.doit = false;
			}
		});
		cboTabela.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selecionaTabela();
			}
		});
		cboTabela.setItems(new String[] {});
		cboTabela.setBounds(225, 30, 208, 8);
		
		grpNomeObjetos = new Group(composite_3, SWT.NONE);
		grpNomeObjetos.setBounds(10, 236, 640, 230);
		
		Label label = new Label(grpNomeObjetos, SWT.NONE);
		label.setText("Nome da classe de modelo:");
		label.setBounds(10, 26, 207, 14);
		
		txtNomeClassePojo = new Text(grpNomeObjetos, SWT.BORDER);
		txtNomeClassePojo.setBounds(10, 46, 314, 30);
		txtNomeClassePojo.setTextLimit(80);
		
		Label label_1 = new Label(grpNomeObjetos, SWT.NONE);
		label_1.setText("Nome da classe de persistência:");
		label_1.setBounds(10, 73, 191, 30);
		
		txtNomeClasseDao = new Text(grpNomeObjetos, SWT.BORDER);
		txtNomeClasseDao.setBounds(10, 93, 314, 30);
		txtNomeClasseDao.setTextLimit(80);
		
		Label label_2 = new Label(grpNomeObjetos, SWT.NONE);
		label_2.setText("Nome da classe de serviço:");
		label_2.setBounds(10, 120, 191, 15);
		
		txtNomeClasseNegocio = new Text(grpNomeObjetos, SWT.BORDER);
		txtNomeClasseNegocio.setBounds(10, 140, 314, 30);
		txtNomeClasseNegocio.setTextLimit(80);
		
		Label label_3 = new Label(grpNomeObjetos, SWT.NONE);
		label_3.setText("Nome da classe de fachada:");
		label_3.setBounds(10, 167, 191, 15);
		
		txtNomeClasseVisao = new Text(grpNomeObjetos, SWT.BORDER);
		txtNomeClasseVisao.setBounds(10, 187, 314, 30);
		txtNomeClasseVisao.setTextLimit(80);
		
		Label lblDescrioDoCaso = new Label(composite_3, SWT.NONE);
		lblDescrioDoCaso.setText("Descrição do web service");
		lblDescrioDoCaso.setBounds(74, 71, 468, 30);
		
		txtNomeCasoUso = new Text(composite_3, SWT.BORDER);
		txtNomeCasoUso.setBounds(10, 80, 81, 40);
		txtNomeCasoUso.setTextLimit(80);
		
		Label lblHomeDoProjeto = new Label(composite_3, SWT.NONE);
		lblHomeDoProjeto.setText("Localização do projeto");
		lblHomeDoProjeto.setBounds(14, 147, 468, 30);
		
		txtHomeProjeto = new Text(composite_3, SWT.BORDER);
		txtHomeProjeto.setEditable(false);
		txtHomeProjeto.setBounds(93, 161, 449, 30);
		txtHomeProjeto.setTextLimit(80);
		
		Button btnAbrirHomeProject = new Button(composite_3, SWT.NONE);
		btnAbrirHomeProject.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				abrirProjectHome();
			}
		});
//		btnAbrirHomeProject.setImage(SWTResourceManager.getImage("C:\\java\\desenv7teste\\UseCase\\icons\\button-open.png"));
		btnAbrirHomeProject.setBounds(463, 122, 27, 30);
		
		Label lblSiglaProj = new Label(composite_3, SWT.NONE);
		lblSiglaProj.setText("Sigla Projeto");
		lblSiglaProj.setBounds(513, 107, 130, 15);
		
		txtSiglaProjeto = new Text(composite_3, SWT.BORDER);
		txtSiglaProjeto.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				setDefaultConfig();
			}
		});
		txtSiglaProjeto.setBounds(513, 127, 137, 30);
		txtSiglaProjeto.setTextLimit(10);
		
		//////////////////////////////////////////////////////////////
		
		TabItem tabMapeamento = new TabItem(tabFolder, SWT.NONE);
		tabMapeamento.setText("Mapeamento Objeto-Relacional do modelo");
		
		Composite composite = new Composite(tabFolder, SWT.NONE);
		tabMapeamento.setControl(composite);
		
		grdMapeamento = new Table(composite, SWT.BORDER | SWT.MULTI);
		grdMapeamento.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String nomeCampo = ((TableItem)e.item).getText();
				CampoInfo c = findCampo(nomeCampo);
				if (c != null){
					editaDefinicaoCampo(c);
				}
			}
		});
		grdMapeamento.setLinesVisible(true);
		grdMapeamento.setHeaderVisible(true);
		grdMapeamento.setBounds(10, 10, 640, 275);
		
		TableColumn tableColumn = new TableColumn(grdMapeamento, SWT.LEFT);
		tableColumn.setWidth(200);
		tableColumn.setText("Nome do campo");
		
		TableColumn tableColumn_1 = new TableColumn(grdMapeamento, SWT.CENTER);
		tableColumn_1.setWidth(90);
		tableColumn_1.setText("Tipo do banco");
		
		TableColumn tableColumn_2 = new TableColumn(grdMapeamento, SWT.CENTER);
		tableColumn_2.setWidth(100);
		tableColumn_2.setText("Tipo do Java");
		
		TableColumn tableColumn_3 = new TableColumn(grdMapeamento, 0);
		tableColumn_3.setWidth(200);
		tableColumn_3.setText("Caption");
		
		Group group_2 = new Group(composite, SWT.NONE);
		group_2.setBounds(10, 287, 640, 204);
		
		Label lblMaxLength = new Label(group_2, SWT.NONE);
		lblMaxLength.setText("Max Length:");
		lblMaxLength.setBounds(10, 52, 67, 15);
		
		txtMaxLength = new Text(group_2, SWT.BORDER);
		txtMaxLength.setTextLimit(3);
		txtMaxLength.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if ((e.character >= '0' && e.character <= '9') || e.character == '\n' || e.character == '\b') {  
					          e.doit = true;
					          return;
				} 
				e.doit = false;
			}
		});
		txtMaxLength.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				salvaDefinicaoCampoAtual();
			}
		});
		txtMaxLength.setBounds(85, 49, 42, 21);
		Label lblCaption = new Label(group_2, SWT.NONE);
		lblCaption.setText("Caption:");
		lblCaption.setBounds(10, 25, 57, 30);
		
		txtCaption = new Text(group_2, SWT.BORDER);
		txtCaption.setTextLimit(120);
		txtCaption.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				if (((Text)arg0.widget).isFocusControl()){
					salvaDefinicaoCampoAtual();
					if (grdMapeamento.getSelectionIndex() >= 0 && editaCampo != null){
						try{
							grdMapeamento.getSelection()[grdMapeamento.getSelectionIndex()].setText(3, editaCampo.caption);
						}catch (ArrayIndexOutOfBoundsException e){
							
						}
					}
				}
			}
		});
		txtCaption.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				salvaDefinicaoCampoAtual();
			}
		});
		txtCaption.setBounds(85, 22, 208, 30);
		
		Label lblPermiteNulo = new Label(group_2, SWT.NONE);
		lblPermiteNulo.setText("Permite valor nulo:");
		lblPermiteNulo.setBounds(308, 52, 72, 30);
		
		Label lblRequerido = new Label(group_2, SWT.NONE);
		lblRequerido.setText("É um campo obrigatório:");
		lblRequerido.setBounds(135, 52, 70, 30);
		
		cboObrigatorio = new CCombo(group_2, SWT.BORDER);
		cboObrigatorio.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				e.doit = false;
			}
		});
		cboObrigatorio.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				salvaDefinicaoCampoAtual();
			}
		});
		cboObrigatorio.setItems(new String[] {"Sim", "Não"});
		cboObrigatorio.setBounds(207, 49, 82, 30);
		
		cboPermiteNulo = new CCombo(group_2, SWT.BORDER);
		cboPermiteNulo.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				e.doit = false;
			}
		});
		cboPermiteNulo.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				salvaDefinicaoCampoAtual();
			}
		});
		cboPermiteNulo.setItems(new String[] {"Sim", "Não"});
		cboPermiteNulo.setBounds(399, 49, 85, 30);
		
		Label lblNomeVarivel = new Label(group_2, SWT.NONE);
		lblNomeVarivel.setText("Nome Variável:");
		lblNomeVarivel.setBounds(308, 25, 88, 15);
		
		txtVar = new Text(group_2, SWT.BORDER);
		txtVar.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				salvaDefinicaoCampoAtual();
			}
		});
		txtVar.setBounds(399, 22, 208, 30);
		txtVar.setTextLimit(60);
		
		Label lblRegra = new Label(group_2, SWT.NONE);
		lblRegra.setText("Regra:");
		lblRegra.setBounds(10, 79, 70, 15);
		
		cboRegra = new CCombo(group_2, SWT.BORDER);
		cboRegra.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				e.doit = false;
			}
		});
		cboRegra.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				salvaDefinicaoCampoAtual();
			}
		});
		cboRegra.setItems(new String[] {"String livre", "String caixa alta", "E-mail", "Número", "Número com intervalo definido"});
		cboRegra.setBounds(85, 76, 399, 21);
		
		txtValorDefault = new Text(group_2, SWT.BORDER);
		txtValorDefault.setTextLimit(60);
		txtValorDefault.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				salvaDefinicaoCampoAtual();
			}
		});
		txtValorDefault.setBounds(85, 104, 399, 30);
		
		Label lblValorDefault = new Label(group_2, SWT.NONE);
		lblValorDefault.setText("Valor default:");
		lblValorDefault.setBounds(10, 107, 72, 15);
		
		txtMax = new Text(group_2, SWT.BORDER);
		txtMax.setTextLimit(3);
		txtMax.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if ((e.character >= '0' && e.character <= '9') || e.character == '\n' || e.character == '\b') {  
			          e.doit = true;
			          return;
				} 
				e.doit = false;
			}
		});
		txtMax.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				salvaDefinicaoCampoAtual();
			}
		});
		txtMax.setBounds(145, 131, 42, 30);
		
		Label lblValorMx = new Label(group_2, SWT.NONE);
		lblValorMx.setText("Min/Max:");
		lblValorMx.setBounds(10, 134, 67, 15);
		
		txtMin = new Text(group_2, SWT.BORDER);
		txtMin.setTextLimit(3);
		txtMin.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if ((e.character >= '0' && e.character <= '9') || e.character == '\n' || e.character == '\b') {  
			          e.doit = true;
			          return;
				} 
				e.doit = false;
			}
		});
		txtMin.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				salvaDefinicaoCampoAtual();
			}
		});
		txtMin.setBounds(85, 131, 42, 30);
		
		Label label_4 = new Label(group_2, SWT.NONE);
		label_4.setText("-");
		label_4.setBounds(135, 134, 17, 15);
		
		Label lblWidget = new Label(group_2, SWT.NONE);
		lblWidget.setText("Widget:");
		lblWidget.setBounds(10, 163, 70, 15);
		
		cboWidget = new CCombo(group_2, SWT.BORDER);
		cboWidget.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				e.doit = false;
			}
		});
		cboWidget.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				salvaDefinicaoCampoAtual();
			}
		});
		cboWidget.setItems(new String[] {"InputText", "ComboBox", "Calendar", "Memo"});
		cboWidget.setBounds(85, 160, 171, 30);
		
		Label lblFiltrvel = new Label(group_2, SWT.NONE);
		lblFiltrvel.setText("Filtrável:");
		lblFiltrvel.setBounds(204, 134, 46, 15);
		
		cboFiltravel = new CCombo(group_2, SWT.BORDER);
		cboFiltravel.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				e.doit = false;
			}
		});
		cboFiltravel.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				salvaDefinicaoCampoAtual();
			}
		});
		cboFiltravel.setItems(new String[] {"Sim", "Não"});
		cboFiltravel.setBounds(256, 131, 85, 21);
		
		Label lblMapear = new Label(group_2, SWT.NONE);
		lblMapear.setText("Mapear:");
		lblMapear.setBounds(348, 134, 46, 15);
		
		cboMapear = new CCombo(group_2, SWT.BORDER);
		cboMapear.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				e.doit = false;
			}
		});
		cboMapear.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				salvaDefinicaoCampoAtual();
			}
		});
		cboMapear.setItems(new String[] {"Sim", "Não"});
		cboMapear.setBounds(399, 131, 85, 30);
		
		TabItem Sumário = new TabItem(tabFolder, SWT.NONE);
		Sumário.setText("Sumário");
		
		TextViewer textViewer = new TextViewer(tabFolder, SWT.BORDER);
		txtScript = textViewer.getTextWidget();
		txtScript.setEditable(false);
		txtScript.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				geraScritJson();
			}
		});
		Sumário.setControl(txtScript);
		
		Button btnNewButton = new Button(shell, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.exit(0);
			}
		});
		btnNewButton.setText("&Sair");
		
		Button btnVerPendencias = new Button(shell, SWT.NONE);
		btnVerPendencias.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				verificarPendencias();
			}
		});
		btnVerPendencias.setText("&Verificar Pendências");

		Button btnGerarCasoDe = new Button(shell, SWT.NONE);
		btnGerarCasoDe.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				geraCasoUso();
			}
		});
		btnGerarCasoDe.setText("&Gerar");
		
	}
	
	public NewWebServiceForm() throws Exception{
		

		//conectarDatabase();
		//getDatabases();
		//cboTabela.setEnabled(false);
		//grpNomeObjetos.setEnabled(false);
		
		//Label lblSiglaProj_1 = new Label(grpNomeObjetos, SWT.NONE);
		//lblSiglaProj_1.setText("Nome ");
		//lblSiglaProj_1.setBounds(330, 26, 145, 15);
		
		//txtWorkspacePojo = new Text(grpNomeObjetos, SWT.BORDER);
		//txtWorkspacePojo.setBounds(330, 46, 274, 30);
		
		/*try{
			IWorkspace workspace = ResourcesPlugin.getWorkspace(); 
			txtHomeProjeto.setText(workspace.getRoot().getLocation().toFile().getPath().toString());
		}catch (Exception e){
			e.printStackTrace();
		}*/
		 
		
	    
		//geraScritJson();
	}

	private void getDatabases() throws SQLException {
		List<String> l = database.getListaDatabases();
		String[] lista = l.toArray(new String[0]);
		cboDatabase.setItems(lista);
	}

	private void selecionaDatabase(){
		try {
			getListaTabelas();
			cboTabela.setEnabled(true);
			grpNomeObjetos.setEnabled(false);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void getListaTabelas() throws SQLException {
		String nomeDatabase = cboDatabase.getText();
		List<String> l = database.getListaNomeTabela(nomeDatabase);
		String[] lista = l.toArray(new String[0]);
		cboTabela.setItems(lista);
	}
	
	private void selecionaTabela() {
		setDefaultConfig();
		grpNomeObjetos.setEnabled(true);
		getCamposTabelaSelecionada();
	}
	
	private void getCamposTabelaSelecionada() {
		try {
			campos = database.getListaCamposTabela(cboTabela.getText());
			Iterator<CampoInfo> it = campos.iterator(); 
			grdMapeamento.removeAll();
			while (it.hasNext()){
				CampoInfo c = it.next(); 
				TableItem item = new TableItem(grdMapeamento, SWT.NONE);
				item.setText(new String[]{c.nome, c.tipo, c.tipoJava, c.caption});
				
		/*		// Checkbox para a coluna "Disponível Filtro"
				TableEditor editor = new TableEditor (grdMapeamento);
				Button checkButton = new Button(grdMapeamento, SWT.CHECK);
				checkButton.pack();
				editor.minimumWidth = checkButton.getSize ().x;
				editor.horizontalAlignment = SWT.CENTER;
				editor.setEditor(checkButton, item, 3);
				checkButton.setData(c);
				checkButton.addSelectionListener(new SelectionListener() {
					
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						CampoInfo c = (CampoInfo) ((Button) arg0.getSource()).getData();
						c.filtravel = !(c.filtravel);
						cboFiltravel.select(c.filtravel ? 1 : 0);
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent arg0) {
						// TODO Auto-generated method stub
						
					}
				} );
				
				
				// Checkbox para a coluna "Remover Pojo"
				editor = new TableEditor (grdMapeamento);
				checkButton = new Button(grdMapeamento, SWT.CHECK);
				checkButton.pack();
				editor.minimumWidth = checkButton.getSize ().x;
				editor.horizontalAlignment = SWT.CENTER;
				editor.setEditor(checkButton, item, 4);
				checkButton.setData(c);
		*/
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void geraScritJson(){
		StringBuilder script = new StringBuilder();
		script.append("{");
		script.append(String.format("\"usecase_name\":\"%s\",", txtNomeCasoUso.getText()));
		script.append(String.format("\n \"project_home\":\"%s\",", txtHomeProjeto.getText())); 
			script.append("\n \"pojo\":{");
				script.append(String.format("\n\t\t\t   \"workspace\":\"%s\",", txtWorkspacePojo.getText()));	
				script.append(String.format("\n\t\t\t   \"classname\":\"%s\",", txtNomeClassePojo.getText()));
				script.append(String.format("\n\t\t\t   \"tabela\":\"%s\",", cboTabela.getText()));
				
				script.append("\n\t\t\t   \"fields\": {");
				String sVirg = ",";
				for (int i = 0; i <= grdMapeamento.getItemCount()-1; i++){
					TableItem item = grdMapeamento.getItem(i);
					CampoInfo c = findCampo(item.getText());
					if (i == grdMapeamento.getItemCount()-1){
						sVirg = "";
					}
					script.append(String.format("\n\t\t\t\t      \"%s\": ", c.nome));
					script.append(String.format("{ \"type\" : \"%s\", \"max_length\" : \"%s\", \"required\" : \"%s\", \"primarykey\" : \"%s\", \"unique\" : \"%s\", \"nullable\" : \"%s\", \"column\" : \"%s\" , \"varName\" : \"%s\" , \"caption\" : \"%s\" , \"max\" : \"%s\" , \"min\" : \"%s\" , \"valorDefault\" : \"%s\"  , \"regra\" : \"%s\" , \"widget\" : \"%s\" , \"mapear\" : \"%s\" , \"filtravel\" : \"%s\" }%s", 
													c.tipoJava, 
													c.maxlength == null ? "" : c.maxlength, 
													c.obrigatorio ? "true" : "false",
													c.primarykey ? "true" : "false",
													c.unique ? "true" : "false",
													c.permiteNulo ? "true" : "false",
													c.nome,
													c.nomeVar,
													c.caption,
													c.max == null ? "" : c.max,
													c.min == null ? "" : c.min,
													c.valorDefault,
													c.regra,
													c.widget,
													c.mapear ? "true" : "false",
													c.filtravel ? "true" : "false",
													sVirg));
				}
				
				script.append("\n\t\t\t    }");
			script.append("\n\t}");
		script.append("\n}");
		
		txtScript.setText(script.toString());
	}
	
	private void setDefaultConfig(){
		String nomeTabela = cboTabela.getText();
		String nomePojo = "";
		String nomeDao = "";
		String nomeNegocio = "";
		String nomeVisao = "";

		if (nomeTabela.length() > 0){

			if (nomeTabela.substring(0, 3).equals("TB_")){
				nomePojo = nomeTabela.substring(3);	
			}else{
				nomePojo = nomeTabela;
			}
			
			if (nomePojo.length() > 0){
				nomeDao = nomePojo + "DaoImpl";
				nomeNegocio = "Manter" + nomePojo + "NegocioImpl";
				nomeVisao = nomePojo + "Visao";
			}
			
		}
		
		if (txtNomeCasoUso.getText().isEmpty()){
			txtNomeCasoUso.setText("Manter "+ nomePojo);
		}
		
		String dir = txtHomeProjeto.getText();
		if (!dir.isEmpty()){
			int posSigla = dir.lastIndexOf('/');
			if (posSigla >= 0){
				txtSiglaProjeto.setText(dir.substring(posSigla+1));
			}
		}

		txtNomeClassePojo.setText(nomePojo);
		txtNomeClasseDao.setText(nomeDao);
		txtNomeClasseNegocio.setText(nomeNegocio);
		txtNomeClasseVisao.setText(nomeVisao);
		txtWorkspacePojo.setText(String.format("br.unb.web.%s.pojo", txtSiglaProjeto.getText()));
		
		geraScritJson();
	}

	private void conectarDatabase() throws Exception {
		database = new Database();
		database.conecta("BDExtensao", "usuextensao", "usuextensao");
	}

	
	private void abrirProjectHome(){
		
		DirectoryDialog dlg = new DirectoryDialog(shell);
		dlg.setText("Selecionar Home do Projeto");
		dlg.setMessage("Selecione o home do projeto");
		String dir = dlg.open();
        if (dir != null) {
          txtHomeProjeto.setText(dir.replaceAll("\\\\", "/"));
          setDefaultConfig();
        }
	}
	
	private void editaDefinicaoCampo(CampoInfo c){
		salvaDefinicaoCampoAtual();
		editaCampo = c;
		txtCaption.setText(c.caption);
		txtVar.setText(c.nomeVar);
		txtMaxLength.setText(c.maxlength == null ? "" : c.maxlength.toString());
		cboObrigatorio.select(c.obrigatorio ? 1 : 0);
		cboPermiteNulo.select(c.permiteNulo ? 1 : 0);
		cboRegra.select(cboRegra.indexOf(c.regra));
		txtValorDefault.setText(c.valorDefault);
		txtMax.setText(c.max == null ? "" : c.max.toString());
		txtMin.setText(c.min == null ? "" : c.min.toString());
		cboFiltravel.select(c.filtravel ? 1 : 0);
		cboMapear.select(c.mapear ? 1 : 0);
		cboWidget.select(cboWidget.indexOf(c.widget));
	}
	
	private CampoInfo findCampo(String nomeCampo){
		if (campos != null) {
			for (CampoInfo c : campos){
				if (c.nome.equals(nomeCampo)){
					return c;
				}
			}
		}
		return null;
	}
	
	private void salvaDefinicaoCampoAtual() {
		if (editaCampo != null){
			editaCampo.caption = txtCaption.getText();
			editaCampo.nomeVar = txtVar.getText();
			
			try{
				editaCampo.maxlength = Integer.parseInt(txtMaxLength.getText());
			}catch(Exception e){
				editaCampo.maxlength = null;
			}
			
			editaCampo.obrigatorio = cboObrigatorio.getText().equals("Sim");
			editaCampo.permiteNulo = cboPermiteNulo.getText().equals("Sim");
			editaCampo.regra = cboRegra.getText();
			editaCampo.valorDefault = txtValorDefault.getText();
			
			try{
				editaCampo.max = Integer.parseInt(txtMax.getText());
			} catch (Exception e){
				editaCampo.max = null;
			}
			
			try{
				editaCampo.min = Integer.parseInt(txtMin.getText());
			}catch (Exception e){
				editaCampo.min = null;
			}
			
			
			editaCampo.filtravel = cboFiltravel.getText().equals("Sim");
			editaCampo.mapear = cboMapear.getText().equals("Sim");
			editaCampo.widget = cboWidget.getText();
			
			geraScritJson();
		}
	}

	private void verificarPendencias(){
		if (campos != null){
			for (CampoInfo c : campos){
				if (c.max != null && c.min != null){
					if (c.max < c.min){
						messageBox("Verifique as pendências a seguir: \n\nValor máximo se informado deve ser maior que o valor mínimo.");
					}
				}
			}
		}

	}
	
	private void messageBox(String message) {
		MessageDialog dialog = new MessageDialog(shell, "Atenção", null,
			message, MessageDialog.ERROR, new String[] { "Ok" }, 0);
		dialog.open();
	}

	public String salvaPFile() throws IOException{  
		String pfileName = "c:/temp/pfile.ini";
		PrintWriter pw = new PrintWriter (new FileOutputStream(pfileName, false), false);  
		pw.print(txtScript.getText());   
		pw.write(13);  
		pw.write(10);  
		pw.close();
		return pfileName;
	}  

	private String executaComando(String comando) throws IOException{
		File dir = new File("c:/temp");
		Runtime run = Runtime.getRuntime();
		Process proc = run.exec(comando,null,dir);
		InputStream is = proc.getInputStream();
		InputStreamReader isreader = new InputStreamReader(is);
		BufferedReader input = new BufferedReader(isreader);
		List<String> linhas = new ArrayList<String>();
		String linha = "";
		while ((linha = input.readLine()) != null) {
			linhas.add(linha);
		}
		input.close();
		return linhas.toString();
	}
	
	
	private void geraCasoUso(){
		verificarPendencias();
		try {
			String pfileName = salvaPFile();
			String comando = "python C:/eclipse/eclipse_workspace/usecase/usecase.py pfile="+ pfileName;
			String retorno = executaComando(comando);
			messageBox(retorno);
		} catch (IOException e) {
			e.printStackTrace();
			messageBox(e.getMessage());
		}
	}
	

	public Table grdMapeamento() {
		return grdMapeamento;
	}
	
	public StyledText txtScript() {
		return txtScript;
	}
	public Text txtNomeCasoUso() {
		return txtNomeCasoUso;
	}
	public Text txtHomeProjeto() {
		return txtHomeProjeto;
	}
	public Text txtSiglaProjeto() {
		return txtSiglaProjeto;
	}
	public Text txtWorkspacePojo() {
		return txtWorkspacePojo;
	}
	public CCombo cboObrigatorio() {
		return cboObrigatorio;
	}
	public CCombo cboPermiteNulo() {
		return cboPermiteNulo;
	}
	public CCombo cboRegra() {
		return cboRegra;
	}
	public Text txtValorDefault() {
		return txtValorDefault;
	}
	public Text txtMax() {
		return txtMax;
	}
	public Text txtMin() {
		return txtMin;
	}
	public CCombo cboCombo() {
		return cboFiltravel;
	}
	public CCombo cboMapear() {
		return cboMapear;
	}
	public CCombo cboWidget() {
		return cboWidget;
	}
}
