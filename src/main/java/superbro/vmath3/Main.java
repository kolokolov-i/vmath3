package superbro.vmath3;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.stage.Stage;

import static java.lang.Math.*;

public class Main extends Application {

    private boolean stop = false, done = false;

    private void calculate() {
        stop = false;
        do {
            calculateStep();
        }
        while (!stop);
    }

    private int iteration;
    private final float phi = (float) (1 + sqrt(5)) / 2;

    private void calculateStep() {
        if (stop) {
            return;
        }
        if (iteration > 20) {
            stop = true;
            done = true;
            return;
        }
        float a = -HALF, b = HALF, x1, x2, y1, y2;
        boolean xy = iteration % 2 == 0;
        float lastR = xy ? resX : resY;
        float curR = 0;
        float otherR = xy ? resY : resX;
        boolean flag = true;
        do {
            x1 = b - (b - a) / phi;
            x2 = a + (b - a) / phi;
            if (xy) {
                y1 = f(x1, otherR);
                y2 = f(x2, otherR);
            } else {
                y1 = f(otherR, x1);
                y2 = f(otherR, x2);
            }
            if (y1 >= y2) {
                a = x1;
            } else {
                b = x2;
            }
            if (abs(a - b) < 0.01) {
                curR = (a + b) / 2;
                flag = false;
            }
        }
        while(flag);
        if(xy){
            resX = curR;
        }
        else {
            resY = curR;
        }
        if(abs(curR - lastR) < 0.01){
            stop = true;
        }
        resZ = f(resX, resY);
        iteration++;
    }

    private float resX, resY, resZ;

    private static float f(float x, float y) {
        double tx, ty, r;
        tx = x * 0.7 - y * 0.7;
        ty = x * 0.7 + y * 0.7;
        tx += 20;
        ty -= 50;
        tx *= 0.1;
        ty *= 0.1;
        x *= 0.1;
        y *= 0.1;
        r = sqrt(tx * tx + ty * ty * 8);
        return (float) ((x * x + y * y) * 0.3 - 20 * exp(-r * r / 32));
    }

    final Group root = new Group();
    final Xform world = new Xform();
    final Xform frameGroup = new Xform();
    final Xform surfaceGroup = new Xform();
    final Xform resultGroup = new Xform();
    final PerspectiveCamera camera = new PerspectiveCamera(true);
    final Xform cameraXform = new Xform();
    final Xform cameraXform2 = new Xform();
    final Xform cameraXform3 = new Xform();

    @Override
    public void start(Stage primaryStage) throws Exception {
        root.getChildren().add(world);
        Scene scene = new Scene(root, 1024, 768, true);
        scene.setFill(Color.WHITE);
        handleKeyboard(scene, world);
        handleMouse(scene, world);
        scene.setCamera(camera);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Extremum finder");
        buildCamera();
        buildFrame();
        buildSurface();
        buildResult();
        world.getChildren().addAll(frameGroup, surfaceGroup, resultGroup);
        resetResult();
        primaryStage.show();
    }

    Sphere kapel;

    private void buildResult() {
        kapel = new Sphere(3);
        final PhongMaterial kapelMaterial = new PhongMaterial();
        kapelMaterial.setDiffuseColor(Color.DEEPSKYBLUE);
        kapelMaterial.setSpecularColor(Color.DEEPSKYBLUE);
        kapel.setMaterial(kapelMaterial);
        resultGroup.getChildren().addAll(kapel);
        resultGroup.setVisible(true);
    }

    private void resetResult() {
        resX = -50;
        resY = -80;
        resZ = f(resX, resY);
        updateKapel();
        done = false;
        iteration = 0;
    }

    private void updateKapel() {
        kapel.setTranslateX(resX);
        kapel.setTranslateY(resZ);
        kapel.setTranslateZ(resY);
    }

    private static final double CAMERA_INITIAL_DISTANCE = -450;
    private static final double CAMERA_INITIAL_X_ANGLE = 50.0;
    private static final double CAMERA_INITIAL_Y_ANGLE = 320.0;
    private static final double CAMERA_NEAR_CLIP = 0.1;
    private static final double CAMERA_FAR_CLIP = 10000.0;

    private void buildCamera() {
        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
        cameraXform3.setRotateZ(180.0);
        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
    }

    private static final float FRAME_LENGTH = 200;
    private static final float HALF = 100;

    private void buildFrame() {
        final PhongMaterial frameMaterial = new PhongMaterial();
        frameMaterial.setDiffuseColor(Color.GRAY);
        frameMaterial.setSpecularColor(Color.GRAY);
        final Box frameXA = new Box(FRAME_LENGTH, 1, 1);
        final Box frameZA = new Box(1, 1, FRAME_LENGTH);
        final Box frameXB = new Box(FRAME_LENGTH, 1, 1);
        final Box frameZB = new Box(1, 1, FRAME_LENGTH);
        frameXA.setTranslateZ(HALF);
        frameXB.setTranslateZ(-HALF);
        frameZA.setTranslateX(HALF);
        frameZB.setTranslateX(-HALF);
        frameXA.setMaterial(frameMaterial);
        frameZA.setMaterial(frameMaterial);
        frameXB.setMaterial(frameMaterial);
        frameZB.setMaterial(frameMaterial);
        frameGroup.getChildren().addAll(frameXA, frameZA, frameXB, frameZB);
        frameGroup.setVisible(true);
    }

    private MeshView surfaceMeshView;
    private static final int DETALISATION = 100;
    private final int pointsLineCount = DETALISATION + 1;
    private int pointsCount = pointsLineCount * pointsLineCount;
    private int facesCount = DETALISATION * DETALISATION * 2;
    float[] points = new float[pointsCount * 3];
    int[] faces = new int[facesCount * 12];

    private void buildSurface() {
        float x, y, d;
        d = FRAME_LENGTH / DETALISATION;
        y = -HALF;
        int p = 0;
        for (int i = 0; i <= DETALISATION; i++) {
            x = -HALF;
            for (int j = 0; j <= DETALISATION; j++) {
                points[p++] = x;
                points[p++] = f(x, y);
                points[p++] = y;
                x += d;
            }
            y += d;
        }
        p = 0;
        for (int i = 0; i < DETALISATION; i++) {
            for (int j = 0; j < DETALISATION; j++) {
                int t1 = pointsLineCount * i + j;
                int t2 = pointsLineCount * i + j + 1;
                int t3 = pointsLineCount * (i + 1) + j;
                int t4 = pointsLineCount * (i + 1) + j + 1;
                // triangle A
                faces[p++] = t1;
                faces[p++] = 0;
                faces[p++] = t3;
                faces[p++] = 0;
                faces[p++] = t2;
                faces[p++] = 0;
//                 triangle B
                faces[p++] = t2;
                faces[p++] = 0;
                faces[p++] = t3;
                faces[p++] = 0;
                faces[p++] = t4;
                faces[p++] = 0;
                // triangle A
                faces[p++] = t2;
                faces[p++] = 0;
                faces[p++] = t3;
                faces[p++] = 0;
                faces[p++] = t1;
                faces[p++] = 0;
//                 triangle B
                faces[p++] = t4;
                faces[p++] = 0;
                faces[p++] = t3;
                faces[p++] = 0;
                faces[p++] = t2;
                faces[p++] = 0;
            }
        }
        TriangleMesh surfaceMesh = new TriangleMesh(VertexFormat.POINT_TEXCOORD);
        surfaceMesh.getPoints().addAll(points);
        surfaceMesh.getTexCoords().addAll(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        surfaceMesh.getFaces().addAll(faces);
        final PhongMaterial surfaceMaterial = new PhongMaterial();
        surfaceMaterial.setDiffuseColor(Color.LIGHTCORAL);
        surfaceMaterial.setSpecularColor(Color.LIGHTCORAL);
        surfaceMeshView = new MeshView();
        surfaceMeshView.setMesh(surfaceMesh);
        surfaceMeshView.setMaterial(surfaceMaterial);
        surfaceGroup.getChildren().add(surfaceMeshView);
    }

    double mousePosX;
    double mousePosY;
    double mouseOldX;
    double mouseOldY;
    double mouseDeltaX;
    double mouseDeltaY;

    private static final double SHIFT_MULTIPLIER = 10.0;
    private static final double MOUSE_SPEED = 1.0;
    private static final double ROTATION_SPEED = 0.5;
    private static final double TRACK_SPEED = 1.0;

    private void handleMouse(Scene scene, final Node root) {
        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseOldX = me.getSceneX();
                mouseOldY = me.getSceneY();
            }
        });
        scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                mouseOldX = mousePosX;
                mouseOldY = mousePosY;
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseDeltaX = (mousePosX - mouseOldX);
                mouseDeltaY = (mousePosY - mouseOldY);
                double modifier = 1.0;
                if (me.isShiftDown()) {
                    modifier = SHIFT_MULTIPLIER;
                }
                if (me.isPrimaryButtonDown()) {
                    cameraXform.ry.setAngle(cameraXform.ry.getAngle() -
                            mouseDeltaX * modifier * ROTATION_SPEED);  //
                    cameraXform.rx.setAngle(cameraXform.rx.getAngle() +
                            mouseDeltaY * modifier * ROTATION_SPEED);  // -
                } else if (me.isSecondaryButtonDown()) {
                    double z = camera.getTranslateZ();
                    double newZ = z + mouseDeltaY * MOUSE_SPEED * modifier;
                    camera.setTranslateZ(newZ);
                } else if (me.isMiddleButtonDown()) {
                    cameraXform2.t.setX(cameraXform2.t.getX() +
                            mouseDeltaX * MOUSE_SPEED * modifier * TRACK_SPEED);  // -
                    cameraXform2.t.setY(cameraXform2.t.getY() +
                            mouseDeltaY * MOUSE_SPEED * modifier * TRACK_SPEED);  // -
                }
            }
        });
    }

    private void handleKeyboard(Scene scene, final Node root) {
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case Z:
                        cameraXform2.t.setX(0.0);
                        cameraXform2.t.setY(0.0);
                        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
                        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
                        resetResult();
                        break;
                    case X:
                        frameGroup.setVisible(!frameGroup.isVisible());
                        break;
//                    case V:
//                        resultGroup.setVisible(!resultGroup.isVisible());
//                        break;
                    case A:
                        surfaceMeshView.setDrawMode(DrawMode.FILL);
                        break;
                    case S:
                        surfaceMeshView.setDrawMode(DrawMode.LINE);
                        break;
                    case SPACE:
                        if (event.isControlDown()) {
                            calculateStep();
                        } else {
                            calculate();
                        }
                        updateKapel();
                }
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
