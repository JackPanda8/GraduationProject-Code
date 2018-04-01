/**
 * Created by Administrator on 2018/1/28.
 */
public class FieldVector {

    private double similarity0;
    private double similarity1;
    private double similarity2;
    private double similarity3;
    private double similarity4;
    private double similarity5;
    private double similarity6;
    private double similarity7;
    private double similarity8;
    private double similarity9;
    private double similarity10;
    private double similarity11;
    private double similarity12;
    private double similarity13;
    private double similarity14;
    private double similarity15;
    private double similarity16;

    public FieldVector() {

    }

    public FieldVector(double similarity0, double similarity1, double similarity2, double similarity3, double similarity4, double similarity5, double similarity6, double similarity7, double similarity8, double similarity9, double similarity10, double similarity11, double similarity12, double similarity13, double similarity14, double similarity15, double similarity16) {
        this.similarity0 = similarity0;
        this.similarity1 = similarity1;
        this.similarity2 = similarity2;
        this.similarity3 = similarity3;
        this.similarity4 = similarity4;
        this.similarity5 = similarity5;
        this.similarity6 = similarity6;
        this.similarity7 = similarity7;
        this.similarity8 = similarity8;
        this.similarity9 = similarity9;
        this.similarity10 = similarity10;
        this.similarity11 = similarity11;
        this.similarity12 = similarity12;
        this.similarity13 = similarity13;
        this.similarity14 = similarity14;
        this.similarity15 = similarity15;
        this.similarity16 = similarity16;
    }

    public double getSimilarity0() {
        return similarity0;
    }

    public void setSimilarity0(double similarity0) {
        this.similarity0 = similarity0;
    }

    public double getSimilarity1() {
        return similarity1;
    }

    public void setSimilarity1(double similarity1) {
        this.similarity1 = similarity1;
    }

    public double getSimilarity2() {
        return similarity2;
    }

    public void setSimilarity2(double similarity2) {
        this.similarity2 = similarity2;
    }

    public double getSimilarity3() {
        return similarity3;
    }

    public void setSimilarity3(double similarity3) {
        this.similarity3 = similarity3;
    }

    public double getSimilarity4() {
        return similarity4;
    }

    public void setSimilarity4(double similarity4) {
        this.similarity4 = similarity4;
    }

    public double getSimilarity5() {
        return similarity5;
    }

    public void setSimilarity5(double similarity5) {
        this.similarity5 = similarity5;
    }

    public double getSimilarity6() {
        return similarity6;
    }

    public void setSimilarity6(double similarity6) {
        this.similarity6 = similarity6;
    }

    public double getSimilarity7() {
        return similarity7;
    }

    public void setSimilarity7(double similarity7) {
        this.similarity7 = similarity7;
    }

    public double getSimilarity8() {
        return similarity8;
    }

    public void setSimilarity8(double similarity8) {
        this.similarity8 = similarity8;
    }

    public double getSimilarity9() {
        return similarity9;
    }

    public void setSimilarity9(double similarity9) {
        this.similarity9 = similarity9;
    }

    public double getSimilarity10() {
        return similarity10;
    }

    public void setSimilarity10(double similarity10) {
        this.similarity10 = similarity10;
    }

    public double getSimilarity11() {
        return similarity11;
    }

    public void setSimilarity11(double similarity11) {
        this.similarity11 = similarity11;
    }

    public double getSimilarity12() {
        return similarity12;
    }

    public void setSimilarity12(double similarity12) {
        this.similarity12 = similarity12;
    }

    public double getSimilarity13() {
        return similarity13;
    }

    public void setSimilarity13(double similarity13) {
        this.similarity13 = similarity13;
    }

    public double getSimilarity14() {
        return similarity14;
    }

    public void setSimilarity14(double similarity14) {
        this.similarity14 = similarity14;
    }

    public double getSimilarity15() {
        return similarity15;
    }

    public void setSimilarity15(double similarity15) {
        this.similarity15 = similarity15;
    }

    public double getSimilarity16() {
        return similarity16;
    }

    public void setSimilarity16(double similarity16) {
        this.similarity16 = similarity16;
    }

    public double getAttributeByIndex(int index) {
        double r = 0.0;
        switch(index) {
            case 0 : {
                r = getSimilarity0();
                break;
            }
            case  1: {
                r = getSimilarity1();
                break;
            }
            case  2: {
                r = getSimilarity2();
                break;
            }
            case  3: {
                r = getSimilarity3();
                break;
            }
            case  4: {
                r = getSimilarity4();
                break;
            }
            case  5: {
                r = getSimilarity5();
                break;
            }
            case  6: {
                r = getSimilarity6();
                break;
            }
            case  7: {
                r = getSimilarity7();
                break;
            }
            case  8: {
                r = getSimilarity8();
                break;
            }
            case  9: {
                r = getSimilarity9();
                break;
            }
            case  10: {
                r = getSimilarity10();
                break;
            }
            case  11: {
                r = getSimilarity11();
                break;
            }
            case  12: {
                r = getSimilarity12();
                break;
            }
            case  13: {
                r = getSimilarity13();
                break;
            }
            case  14: {
                r = getSimilarity14();
                break;
            }
            case  15: {
                r = getSimilarity15();
                break;
            }
            case  16: {
                r = getSimilarity16();
                break;
            }

            default: {
                r =  0.0;
                break;
            }
        }
        return r;
    }

}
