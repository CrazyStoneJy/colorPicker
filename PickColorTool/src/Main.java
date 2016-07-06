
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.awt.*;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

public class Main extends Application {

    private static boolean isEnd = true;
    private static String[] mType = new String[]{"HTML", "RGB", "HEX"};
    private static int mSelectedIndex = 0;

    public static void main(String[] args) {
        System.out.println("Hello World!");
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        //初始化stage
        Rectangle colorRect = new Rectangle(100, 50, 100, 60);
        TextField colorNumber = new TextField("----");
        Label colorLabel = new Label("--");
//        TextField hexColor = new TextField("hexColor");
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(mType);
        comboBox.setValue(mType[mSelectedIndex]);
        comboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                mSelectedIndex = chartAtInArray(mType, newValue);
                System.out.println("index:"+mSelectedIndex);
                colorNumber.requestFocus();
            }
        });

        FlowPane pane = new FlowPane(Orientation.VERTICAL);
        pane.setVgap(10);
        ObservableList<Node> list = pane.getChildren();
        list.add(colorRect);
        list.add(comboBox);
        list.addAll(colorLabel);
        list.add(colorNumber);
        Scene scene = new Scene(pane, 300, 300);
        primaryStage.setTitle("colorPicker");
        primaryStage.setScene(scene);
        primaryStage.show();
        scanColor(colorNumber, colorRect, colorLabel,comboBox);

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                isEnd = false;
                System.out.println(">>>>>>exit");
                System.exit(0);
            }
        });
        //不加这句话keybroad无效
        colorNumber.requestFocus();
    }

    /**
     * 开辟一个线程来执行鼠标选择颜色任务
     *
     * @param colorNumber color显示的数值(根据type的不同会进行改变)
     * @param rectColor   填充颜色的矩形
     */
    private static void scanColor(TextField colorNumber, Rectangle rectColor, Label colorLabel,ComboBox<String> comboBox) {
        new Thread(() -> {
            while (isEnd) {
                try {
                    //获取鼠标经过坐标的色值
                    Robot robot = new Robot();
                    Point point = MouseInfo.getPointerInfo().getLocation();
                    Color pixel = robot.getPixelColor((int) point.getX(), (int) point.getY());
//                    StringBuilder colorString = new StringBuilder();
//                    colorString.append("#").append(Integer.toHexString(pixel.getRed())).append(Integer.toHexString(pixel.getGreen())).append(Integer.toHexString(pixel.getBlue()));
//                    System.out.println("red:"+Integer.toHexString(pixel.getRed())+",green:"+Integer.toHexString(pixel.getGreen())+",blue:"+Integer.toHexString(pixel.getBlue()));
//                    System.out.println("red:"+pixel.getRed()+",green:"+pixel.getGreen()+",blue:"+pixel.getBlue());
//                    System.out.println("colorString: "+colorString);

                    //加入fx application thread的队列,来更新label，否则会有 Not on FX application thread;异常
                    Platform.runLater(() -> {
                        colorLabel.setText(mSelectedIndex != -1 ? getColorStringByType(mSelectedIndex, pixel) : "无效的index");
                        colorNumber.setOnKeyPressed((e) -> {
                            switch (e.getCode()) {
                                case ALT:
                                    System.out.println("click alt");
                                    copyString(colorLabel.getText());
                                    colorNumber.setText(colorLabel.getText());
                                    break;
                                case UP:
                                    System.out.println("up>>>>>");
                                    break;
                                default:
                                    System.out.println("非法的按键");
                                    break;
                            }
                        });

                        rectColor.setFill(javafx.scene.paint.Color.color(((double) pixel.getRed() / 255d), ((double) pixel.getGreen() / 255d), ((double) pixel.getBlue() / 255d)));
                    });
                } catch (AWTException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 根据不同的类型来显示不同颜色的显示方式
     * @param type  类型（目前仅支持hex，rgb,html）
     * @param pixel  color
     * @return
     */
    public static String getColorStringByType(int type, Color pixel) {
        StringBuilder colorString = new StringBuilder();
        switch (type) {
            case 0:
                colorString.append("#").append(Integer.toHexString(pixel.getRed())).append(Integer.toHexString(pixel.getGreen())).append(Integer.toHexString(pixel.getBlue()));
                break;
            case 1:
                colorString.append(pixel.getRed()).append(",").append(pixel.getGreen()).append(",").append(pixel.getBlue());
                break;
            case 2:
                colorString.append("0x").append(Integer.toHexString(pixel.getRed())).append(Integer.toHexString(pixel.getGreen())).append(Integer.toHexString(pixel.getBlue()));
                break;
        }
        return colorString.toString();
    }


    /**
     * 判断一个元素是否在一个数组中
     *
     * @param array 对象数组
     * @param obj   查询的元素
     * @param <T>   泛型
     * @return 返回的元素在数组中位置 (若存在返回数组下标，若不存在返回-1)
     */
    public static <T> int chartAtInArray(T[] array, T obj) {
        if (array == null || array.length == 0) return -1;
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(obj)) return i;
        }
        return -1;
    }

    /**
     * 将string字符串复制到系统的剪切板上（直接用ctrl+v便可黏贴）
     *
     * @param string 要复制在剪切板上的字符串
     */
    public static void copyString(String string) {
        StringSelection selection = new StringSelection(string);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    }


}
