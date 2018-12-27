
import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.AbstractMap.Entry;
import static java.util.AbstractMap.SimpleEntry;


/**
 * Usage of this class:
 *
 * To create a FormPanel object that you can add to any container like a JFrame or other JPanel, you have to
 * provide the name of the class whose objects will be stored and pass the class information to the constructor.
 * The class information is accessed using the name of the class followed by .class, such as Cat.class or Student.class.
 *
 * The basic usage (to create a FormPanel of Student objects) inside some other JPanel class is as follows:
 *
 * <code>
 *     private FormPanel<Student> studentform = new FormPanel<Student>(Student.class);
 *
 *     public ExampleFrame() {
 *         setLayout(null);
 *         studentform.setBounds(250, 200, 400, 400);
 *         this.add(studentform);
 *     }
 *
 *     public List<Student> getFormData() {
 *         List<Student> output = studentform.getItems();
 *         return output;
 *     }
 *
 * </code>
 *
 *
 *
 * @param <T>
 */
@SuppressWarnings("unchecked")
public class FormPanel<T> extends JPanel implements ActionListener {

    private static final int TEXTFIELD_SIZE = 15;
    private static final int HEIGHT_PER_ITEM = 50;

    private boolean finishedButton = false;
    private boolean clearOnAdd = true;

    Logger log = Logger.getLogger("gui");

    protected Class<T> inferredClass = null;

    private List<JComponent> inputs = new LinkedList<>();

    private List<JLabel> labels = new LinkedList<>();
    private List<FormInformation> elements = new LinkedList<>();
    private JButton submitButton = new JButton("Add Item");


    private List<T> items = new LinkedList<>();
    private DefaultListModel<T> listModel = new DefaultListModel<>();
    private JList<T> list = new JList<>(listModel);
    private JButton deleteButton = new JButton("Delete Selected Item");

    // used if created outside graphics context
    private JFrame tmpFrame = null;

    private boolean done = false;

    public FormPanel() {
        this((Class<T>)Dummy.class);
    }

    public FormPanel(Class<T> cls) {
        if (!SwingUtilities.isEventDispatchThread()) {
            finishedButton = true;
            SwingUtilities.invokeLater(() -> {
                tmpFrame = new JFrame("Input " + cls.getSimpleName());
                tmpFrame.setSize(600, 600);
                tmpFrame.getContentPane().add(this);
                tmpFrame.setVisible(true);
            });
        }
        setLayout(new GridBagLayout());
/*        SimpleEntry<String, Object> values = new SimpleEntry<String, Object>();
        if (values.length > 0) {
            Arrays.stream(values).forEach(v -> log.info(v.toString()));
            if (Arrays.stream(values).anyMatch(s -> s.getKey().equals("clearOnAdd") && s.getValue().equals(Boolean.FALSE))) clearOnAdd = false;

        }
        List<Entry<String, Object>> params = new LinkedList<>();
        if (values.length > 0) Arrays.stream(values).forEach(params::add);
*/
        inferredClass = cls;
        //getGenericClass();
        if (!inferredClass.isAnnotationPresent(Item.class)) {
            log.info("Class should be annotated");
            //getDeclaredAnnotations()
        }
        List<Field> fields = Arrays.stream(inferredClass.getDeclaredFields()).filter(f -> f.isAnnotationPresent(Property.class)).collect(Collectors.toList());
        Class c = inferredClass.getSuperclass();
        log.info(c.getName());
        while (c != null && c.getSuperclass() != null && c.getSuperclass().isAnnotationPresent(Item.class)) {
            log.info(c.getName());
            fields.addAll(Arrays.stream(c.getDeclaredFields()).filter(f -> f.isAnnotationPresent(Property.class)).collect(Collectors.toList()));
            c = c.getSuperclass();
        }
        fields.stream().filter(f -> f.isAnnotationPresent(Property.class)).forEach(f -> elements.add(new FormInformation(f, f.getDeclaredAnnotation(Property.class))));
        elements.sort(null);
        generateComponents();

        GridBagConstraints right = new GridBagConstraints();
        right.weightx = 2.0;
        right.fill = GridBagConstraints.BOTH;
        right.gridwidth = GridBagConstraints.REMAINDER;

        submitButton.setVisible(true);
        submitButton.addActionListener(this);
        add(submitButton, right);

        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(5);
        list.setVisible(true);
        add(list, right);

        deleteButton.setVisible(true);
        add(deleteButton, right);
        deleteButton.addActionListener((e) -> {
            T removeItem = list.getSelectedValue();
            if (removeItem == null) return;
            items.remove(removeItem);
            listModel.removeElement(removeItem);
        });
        if (finishedButton) {
            JButton doneButton = new JButton("DONE");
            Object o = this;
            doneButton.addActionListener((e) -> { synchronized(o) {
                o.notifyAll();
                done = true;
            }});
            add(doneButton, right);
        }
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        setVisible(true);
    }

    public FormPanel(String className) throws ClassNotFoundException {
        this((Class<T>)Class.forName(className));
    }

    public FormPanel(String className, boolean ismain) throws ClassNotFoundException {
        inferredClass = (Class<T>)Class.forName(className);
        List<Field> fields = Arrays.stream(inferredClass.getDeclaredFields()).filter(f -> f.isAnnotationPresent(Property.class)).collect(Collectors.toList());
        Class c = inferredClass.getSuperclass();
        log.info(c.getName());
        while (c != null && c.getSuperclass() != null && c.getSuperclass().isAnnotationPresent(Item.class)) {
            log.info(c.getName());
            fields.addAll(Arrays.stream(c.getDeclaredFields()).filter(f -> f.isAnnotationPresent(Property.class)).collect(Collectors.toList()));
            c = c.getSuperclass();
        }
        fields.stream().forEach(f -> elements.add(new FormInformation(f, f.getDeclaredAnnotation(Property.class))));
        elements.sort(null);
        generateComponents();
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        setVisible(true);
    }

    protected void generateComponents() {
        GridBagConstraints left = new GridBagConstraints();
        left.anchor = GridBagConstraints.EAST;
        GridBagConstraints right = new GridBagConstraints();
        right.weightx = 2.0;
        right.fill = GridBagConstraints.HORIZONTAL;
        right.gridwidth = GridBagConstraints.REMAINDER;
        GridBagConstraints rightBig = new GridBagConstraints();
        rightBig.weightx = 2.0;
        rightBig.fill = GridBagConstraints.HORIZONTAL;
        rightBig.gridwidth = GridBagConstraints.REMAINDER;
        rightBig.ipady = HEIGHT_PER_ITEM*elements.size();
        for (FormInformation f : elements) {
            JLabel label = new JLabel(f.property.value().equals("") ? f.field.getName() : f.property.value());
            label.setVisible(true);
            add(label, left);
            labels.add(label);
            log.info(f.field.getName() + " creating component of type " + f.field.getType().getName());
            JComponent component = getComponent(f);
            if (component == null) continue;

            component.setVisible(true);
            add(component, component instanceof FormPanel ? rightBig : right);
            inputs.add(component);
        }
    }

    public JComponent getComponent(FormInformation f) {
        switch (f.field.getType().getName()) {
            case "java.lang.String":
            case "int":
            case "double": {
                return new JTextField(TEXTFIELD_SIZE);
            }
            case "boolean": {
                return new JCheckBox("", false);
            }
            default: {
                if (f.field.getType().isAnnotationPresent(Item.class)) {
                    FormPanel<?> fp;
                    try {
                        fp = new FormPanel(f.field.getType().getName(), false);
                    } catch (ClassNotFoundException ex) {
                        return null;
                    }
                    return fp;
                }
                if (f.field.getType().isEnum()) {
                    Class x = f.field.getType();
                    return new JComboBox<Object>(f.field.getType().getEnumConstants());
//                    JComboBox<f.field>
                }
            }
        }
        return null;
    }

    public Class<T> getGenericClass() {
        if (inferredClass == null){
            Type mySuperclass = getClass().getGenericSuperclass();
            System.out.println(mySuperclass.getTypeName());
            Type tType = ((ParameterizedType)mySuperclass).getActualTypeArguments()[0];
            log.info(tType.toString());
            String className = tType.toString().split(" ")[1];
            try {
                inferredClass = ((Class<T>)Class.forName(className));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
        return inferredClass;
    }

    public Object returnElement(int index) {
        String returnType = elements.get(index).field.getType().getName();
        switch (returnType) {
            case "java.lang.String": {
                return ((JTextField)inputs.get(index)).getText();
            }
            case "int": {
                try {
                    int x = Integer.parseInt(((JTextField)inputs.get(index)).getText());
                    return x;
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
            case "double": {
                try {
                    double x = Double.parseDouble(((JTextField)inputs.get(index)).getText());
                    return x;
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
            case "boolean": {
                return ((JCheckBox)inputs.get(index)).isSelected();
            }
            default: {
                if (elements.get(index).field.getType().isAnnotationPresent(Item.class)) {
                    return ((FormPanel<?>)inputs.get(index)).currentItem();
                }
                if (elements.get(index).field.getType().isEnum()) {
                    return ((JComboBox<Object>)inputs.get(index)).getSelectedItem();
                }
            }
        }
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final T newitem;
        try {
            newitem = ((Class<T>)inferredClass).getConstructor().newInstance();
        } catch (Exception ex) {
            return;
        }
        elements.forEach(f -> Arrays.stream(inferredClass.getMethods()).filter(m -> m.getName().equals("set"+f.field.getName().substring(0,1).toUpperCase() + f.field.getName().substring(1))).forEach(m -> {
            log.info(m.getName());
            try {
                m.invoke(newitem, returnElement(elements.indexOf(f)));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }));
        System.out.println(newitem);
        listModel.addElement(newitem);
        items.add(newitem);
    }

    public T currentItem() {
        final T newitem;
        try {
            newitem = ((Class<T>)inferredClass).getConstructor().newInstance();
        } catch (Exception ex) {
            return null;
        }
        elements.forEach(f -> Arrays.stream(inferredClass.getMethods()).filter(m -> m.getName().equals("set"+f.field.getName().substring(0,1).toUpperCase() + f.field.getName().substring(1))).forEach(m -> {
            log.info(m.getName());
            try {
                m.invoke(newitem, returnElement(elements.indexOf(f))); //Double.parseDouble(((JTextField)(inputs.get(elements.indexOf(f)))).getText()));
            } catch (Exception ex) { ex.printStackTrace(); }
        }));
        return newitem;
    }


    public List<T> getItems() {
        return items;
    }

    public List<T> waitOnItems() {
        if (tmpFrame == null) {
            log.severe("waitOnItems() should not be called from this context");
            return null;
        }
        try {
            synchronized(this) {
                wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        tmpFrame.setVisible(false);
        tmpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        tmpFrame.dispatchEvent(new WindowEvent(tmpFrame, WindowEvent.WINDOW_CLOSING));
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
        listModel.removeAllElements();
//        listModel.addAll(items);
        for (T item : items) listModel.addElement(item);
    }


    private class FormInformation implements Comparable<FormInformation> {
        Property property;
        Field field;

        public FormInformation(Field field, Property property) {
            log.info(field.getName() + " described as " + property.value());
            this.property = property;
            this.field = field;
        }

        @Override
        public int compareTo(FormInformation o) {
            return this.property.order() - o.property.order();
        }
    }

}

@Item
class Dummy {
    @Property
    String name;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
