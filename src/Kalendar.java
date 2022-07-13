import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Scanner;

/*
    Program do zarz¹dzania uprawami w LiF:YO
    Okreœla czas zbiorów przy uwzglednieniu czasu rzeczywistego i czasu w LiF.
    Ró¿nica czasu: 6h czasu rzeczywistego 1 dzieñ LiF
    Synchronizacja czasó³w ustawiona pod konkretny serwer.

    Wzrost plonów zale¿ny od pogody:
    w 4 zmianie pogody z deszczowej na s³oneczn¹ lub na odwrot, pierwszy wzrost
    w kolejnej 4 zmianie dojrzale zbiory
    w kolejnej 4 zmianie gnicie/zanik plonó³w

    Pogoda:
    1-s³oneczna
    2-deszczowa
    3-pochmurna
    4-œnieg
 */
public class Kalendar extends JPanel {
    private boolean DEBUG = false;
    JTable table ;
    static int dod=0;
    public Kalendar() throws IOException {
        super(new GridLayout(1,0));
        MyTableModel t=new MyTableModel();
        t.inic();
        JPanel topPanel = new JPanel();
        table=new JTable(t);
        JButton b1 = new JButton("Aktualny dzieñ");
        JButton b2 = new JButton("Dodatkowe -");
        JButton b3 = new JButton("Dodatkowe +");
        JLabel label=new JLabel("0");
        b1.setVerticalTextPosition(AbstractButton.CENTER);
        b1.setHorizontalTextPosition(AbstractButton.HORIZONTAL);
        b1.setSize(100,50);
        b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scrollTableToCellAt(table,178,1);
                int hh=LocalTime.now().getHour();
                if(hh>=5&&hh<11)
                    table.setRowSelectionInterval(180, 180);
                else if(hh>=11&&hh<17)
                    table.setRowSelectionInterval(181, 181);
                else if(hh>=17&&hh<23)
                    table.setRowSelectionInterval(182, 182);
                else
                    table.setRowSelectionInterval(183, 183);
            }
        });
        b2.setVerticalTextPosition(AbstractButton.CENTER);
        b2.setHorizontalTextPosition(AbstractButton.HORIZONTAL);
        b2.setSize(100,50);
        b2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              dod--;
              label.setText(String.valueOf(dod));
               try {
                    t.inic();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                table.repaint();
            }
        });
        b3.setVerticalTextPosition(AbstractButton.CENTER);
        b3.setHorizontalTextPosition(AbstractButton.HORIZONTAL);
        b3.setSize(100,50);
        b3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dod++;
                label.setText(String.valueOf(dod));
                try {
                    t.inic();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                table.repaint();
            }
        });
        topPanel.add(b1);
        topPanel.add(b2);
        topPanel.add(b3);

        topPanel.add(label);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(topPanel, BorderLayout.NORTH);

        table.setPreferredScrollableViewportSize(new Dimension(1500, 800));
        table.setFillsViewportHeight(true);
        table.getColumnModel().getColumn(14).setMinWidth(0);
        table.getColumnModel().getColumn(14).setMaxWidth(100);

        table.getColumnModel().getColumn(15).setMinWidth(0);
        table.getColumnModel().getColumn(15).setMaxWidth(100);

        table.getColumnModel().getColumn(16).setMinWidth(0);
        table.getColumnModel().getColumn(16).setMaxWidth(100);

        table.getModel().addTableModelListener(new TableModelListener() {
            //Zapis zaznaczonych roœlin do pliku
            public void tableChanged(TableModelEvent e) {
                System.out.println(table.getValueAt(e.getLastRow(),e.getColumn()));
                PrintWriter zapis = null;
                try {
                    zapis = new PrintWriter("data/plony.txt");
                } catch (FileNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
                for(int i=0;i<table.getRowCount();i++)
                {
                    if(table.getValueAt(i,0)!=null)
                    if((boolean)table.getValueAt(i,6)==true||(boolean)table.getValueAt(i,7)==true||(boolean)table.getValueAt(i,8)==true||(boolean)table.getValueAt(i,9)==true||(boolean)table.getValueAt(i,10)==true||(boolean)table.getValueAt(i,11)==true||(boolean)table.getValueAt(i,12)==true||(boolean)table.getValueAt(i,13)==true)
                    zapis.println(table.getValueAt(i,1)+" "+table.getValueAt(i,6)+" "+table.getValueAt(i,7)+" "+table.getValueAt(i,8)+" "+table.getValueAt(i,9)+" "+table.getValueAt(i,10)+" "+table.getValueAt(i,11)+" "+table.getValueAt(i,12)+" "+table.getValueAt(i,13));

                }
                zapis.print("1000-01-01 true true true true true true true true");
                zapis.close();
            }
        });
        //zaznaczenie aktualnego dnia w zaleÅ¼noÅ›ci od godziny
        int hh=LocalTime.now().getHour();
        if(hh>=5&&hh<11)
            table.setRowSelectionInterval(180, 180);
        else if(hh>=11&&hh<17)
            table.setRowSelectionInterval(181, 181);
        else if(hh>=17&&hh<23)
            table.setRowSelectionInterval(182, 182);
        else
            table.setRowSelectionInterval(183, 183);


        JScrollPane scrollPane = new JScrollPane(table);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer()
        {
            //Zmiana kolorystyki tabeli
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
            {
                final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if(isSelected) {
                    c.setBackground(Color.black);
                    c.setForeground(Color.WHITE);
                }
                else {
                    c.setBackground(row % 8 < 4 ? Color.orange : Color.WHITE);

                    c.setForeground(Color.BLACK);
                }
                return c;
            }

        });
        table.setSelectionBackground(Color.BLACK);
        panel.add(scrollPane);
        scrollTableToCellAt(table,178,1);
        add(panel);
    }
//Wciœniêcie guzika "Aktualny dzieñ" zaznaczaj¹cego odpowiedni wiersz tabeli

    //Funkcja s³u¿¹ca do scrollowania tabeli do wybranego wiersza
    //Funkcja zaporzyczona
    public static void scrollTableToCellAt(JTable table, int row, int column) {
        if (!(table.getParent() instanceof JViewport)) {
            return;
        }
        JViewport viewport = (JViewport) table.getParent();

        // This rectangle is relative to the table where the
        // northwest corner of cell (0,0) is always (0,0).
        Rectangle rect = table.getCellRect(row, column, true);

        // The location of the viewport relative to the table
        Point pt = viewport.getViewPosition();

        // Translate the cell location so that it is relative
        // to the view, assuming the northwest corner of the
        // view is (0,0)
        rect.setLocation(rect.x - pt.x, rect.y - pt.y);

        // Scroll the area into view
        viewport.scrollRectToVisible(rect);
    }
    class MyTableModel extends AbstractTableModel {
        private String[] columnNames = {"Data R",
                "Data LiF",
                "Czas",
                "Pogoda",
                "Data Zbioru",
                "Data Gnicia",
                "Przenica",
                "Marchewka",
                "Kapusta",
                "Len",
                "Ziemnaki",
                "Groszek",
                "Cebula",
                "Winogron",
                "Slonce","Deszcz","Chmury","Suma",
                "Dni do zbiorów",
                "Dni do gnicia"};
        private Object[][] data = new Object[366][20];

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }
        //Funkcja inicjujaca do wypelnienia tebeli danymi
        public void inic() throws IOException {
            String syn1="1045-09-13";//data dnia w LiF
            String syn2="2022-04-19";//Data rzeczywista przypadaj¹ca na dany dzieñ w LiF
            LocalDate syn4 = LocalDate.parse(syn2);
            LocalDate start2 = LocalDate.now();
            LocalDate start3 =  start2.minusDays(45);
            //Ustalanie i synchronizacja wyÅ›wietlanych dat
            Period r= Period.between(syn4,start2);
            long rr1= ChronoUnit.DAYS.between(syn4, start2)*4-180;
            LocalDate start = LocalDate.parse(syn1);
            if(rr1<0)
                start=start.minusDays(Math.abs(rr1));
            else
            start=start.plusDays(rr1);
            LocalDate end = start.plusDays(365);


            ArrayList<Integer> pog=new ArrayList<>();
            class uprawy {String date; boolean a,b,c,d,e,f,g,h;}
            ArrayList<uprawy> upr=new ArrayList<>();

           //Odczyt Pogody
            try {
                File myObj = new File("data/pogoda.txt");
                Scanner myReader = new Scanner(myObj);
                while (myReader.hasNextLine()) {
                   pog.add( myReader.nextInt());
                }
                myReader.close();
            } catch (FileNotFoundException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }

            //Odczyt zaznaczonych upraw
            try {

                File myObj2 = new File("data/plony.txt");
                Scanner myReader2 = new Scanner(myObj2);
                String obj;
                while (myReader2.hasNextLine()) {
                     obj=myReader2.next();
                    if(obj.equals(""))break;
                    uprawy u=new uprawy();
                    u.date=obj;
                    obj=myReader2.next();
                    u.a=Boolean.parseBoolean(obj);
                    obj=myReader2.next();
                    u.b=Boolean.parseBoolean(obj);
                    obj=myReader2.next();
                    u.c=Boolean.parseBoolean(obj);
                    obj=myReader2.next();
                    u.d=Boolean.parseBoolean(obj);
                    obj=myReader2.next();
                    u.e=Boolean.parseBoolean(obj);
                    obj=myReader2.next();
                    u.f=Boolean.parseBoolean(obj);
                    obj=myReader2.next();
                    u.g=Boolean.parseBoolean(obj);
                    obj=myReader2.next();
                    u.h=Boolean.parseBoolean(obj);
                    upr.add(u);
                }
                myReader2.close();
            } catch (FileNotFoundException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
            //Wypelnianie tabeli danymi
            int i=0;
            while (!start.isAfter(end)) {
                if(i==365)break;
                this.data[i][1] = start;

                int dniz=zbior(start.getDayOfYear()-1,pog,pog.get((start.getDayOfYear()-1)%365),0,0,8)-dod;
                int dnig=zbior(start.getDayOfYear()-1,pog,pog.get((start.getDayOfYear()-1)%365),0,0,12)-dod;

                switch (i % 4) {
                        case 0:
                            data[i][0] = start3;
                            data[i][2] = "5.00";
                            data[i][3] = new ImageIcon("data/user"+pog.get((start.getDayOfYear()-1)%365)+".png");
                            data[i][4] = start.plusDays(dniz);
                            data[i][5] = start.plusDays(dnig);
                            break;
                        case 1:
                            data[i][0] = start3;
                            data[i][2] = "11.00";
                            data[i][3] = new ImageIcon("data/user"+pog.get((start.getDayOfYear()-1)%365)+".png");
                            data[i][4] = start.plusDays(dniz);
                            data[i][5] = start.plusDays(dnig);
                            break;
                        case 2:
                            data[i][0] = start3;
                            data[i][2] = "17.00";
                            data[i][3] = new ImageIcon("data/user"+pog.get((start.getDayOfYear()-1)%365)+".png");
                            data[i][4] = start.plusDays(dniz);
                            data[i][5] = start.plusDays(dnig);
                            break;
                        case 3:
                            data[i][0] = start3;
                            data[i][2] = "23.00";
                            data[i][3] = new ImageIcon("data/user"+pog.get((start.getDayOfYear()-1)%366)+".png");
                            data[i][4] = start.plusDays(dniz);
                            data[i][5] = start.plusDays(dnig);
                            start3 = start3.plusDays(1);
                            break;

                    }
                data[i][6]=false;
                data[i][7]=false;
                data[i][8]=false;
                data[i][9]=false;
                data[i][10]=false;
                data[i][11]=false;
                data[i][12]=false;
                data[i][13]=false;
                //Modyfikacja danych o odczytane uprawy
                String test=data[i][1].toString();
                for(int j=0;j<upr.size();j++)
                if(upr.get(j).date.equals(test))
                {
                    data[i][6]=upr.get(j).a;
                    data[i][7]=upr.get(j).b;
                    data[i][8]=upr.get(j).c;
                    data[i][9]=upr.get(j).d;
                    data[i][10]=upr.get(j).e;
                    data[i][11]=upr.get(j).f;
                    data[i][12]=upr.get(j).g;
                    data[i][13]=upr.get(j).h;
                }
                data[i][14]=dni(pog,start.getDayOfYear()-1, dniz,1);
                data[i][15]=dni(pog,start.getDayOfYear()-1, dniz,2);
                data[i][16]=dni(pog,start.getDayOfYear()-1, dniz,3)+dni(pog,start.getDayOfYear()-1, dniz,4);
                data[i][17]=(int)data[i][14]+(int)data[i][15]-(int)data[i][16];
                data[i][18]=zbior(start.getDayOfYear()-1,pog,pog.get((start.getDayOfYear()-1)%365),0,0,8-1);
                data[i][19]=zbior(start.getDayOfYear()-1,pog,pog.get((start.getDayOfYear()-1)%365),0,0,12-1);


                start = start.plusDays(1);
                    i++;

            }
        }
        public int dni(ArrayList<Integer> kal,int dzis,int koniec,int p)
        {
            int sum=0;
            for(int i=0;i<koniec;i++)
            {
                if(kal.get((dzis+i)%365)==p)sum++;
            }

            return sum;
        }
        //Funkcja rekurencyjna do wyliczenia daty zbioru lub zgnicia upraw.
        //Dzieñ roku,lista zmian pogody,pogoda(1 lub 2. Pozostala pomijana w zwiazku z tikami wzrostu), nr tiku wzrostu,ilosc dni od punktu startowego,max ilosc tików
        public int zbior(int data,ArrayList<Integer> kal,int pog,int z,int sum,int end)
        {
            sum++;
            if(pog==1&&kal.get((data+1)%365)==2)
            {
                z++;
                pog=2;
            }
            else
            if(pog==2&&kal.get((data+1)%365)==1)
            {
                z++;
                pog=1;
            }
            else
                if(kal.get((data+1)%365)==2)
                    pog=2;
                else
                    if(kal.get((data+1)%365)==1)
                        pog=1;

            if(z<end)
            return zbior(data+1,kal,pog,z,sum,end);
            else
            return sum;
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            if (col < 2) {
                return false;
            } else {
                return true;
            }
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        public void setValueAt(Object value, int row, int col) {
            if (DEBUG) {
                System.out.println("Setting value at " + row + "," + col
                        + " to " + value
                        + " (an instance of "
                        + value.getClass() + ")");
            }

            data[row][col] = value;
            fireTableCellUpdated(row, col);

            if (DEBUG) {
                System.out.println("New value of data:");
                printDebugData();
            }
        }

        private void printDebugData() {
            int numRows = getRowCount();
            int numCols = getColumnCount();

            for (int i=0; i < numRows; i++) {
                System.out.print("    row " + i + ":");
                for (int j=0; j < numCols; j++) {
                    System.out.print("  " + data[i][j]);
                }
                System.out.println();
            }
            System.out.println("--------------------------");
        }
    }


    private static void createAndShowGUI() throws IOException {
        
        JFrame frame = new JFrame("Kalendar");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Kalendar newContentPane = new Kalendar();
        newContentPane.setOpaque(true);
        frame.setContentPane(newContentPane);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) throws IOException {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    createAndShowGUI();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
