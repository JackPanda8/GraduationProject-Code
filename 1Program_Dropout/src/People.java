import java.util.*;

/*rec_id, culture, sex, age,
  date_of_birth, title, given_name, surname, state,
  suburb, postcode, street_number, address_1, address_2,
  phone_number, soc_sec_id, blocking_number, family_role*/

public class People {

    private String rec_id;
    //    private String rec2_id;
    private String culture;
    private String sex;
    private String age;
    private String date_of_birth;
    private String title;
    private String given_name;
    private String surname;
    private String state;
    private String suburb;
    private String postcode;
    private String street_number;
    private String address_1;
    private String address_2;
    private String phone_number;
    private String soc_sec_id;
    private String blocking_number;
    private String family_role;
    private String sortKey;//排序用的key


    public String getSortKey() {
        return sortKey;
    }

    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }

    public String getRec_id() {
        return rec_id;
    }

    public void setRec_id(String rec_id) {
        this.rec_id = rec_id;
    }

//    public String getRec2_id() {
//        return rec2_id;
//    }
//
//    public void setRec2_id(String rec2_id) {
//        this.rec2_id = rec2_id;
//    }

    public String getCulture() {
        return culture;
    }

    public void setCulture(String culture) {
        this.culture = culture;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(String date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGiven_name() {
        return given_name;
    }

    public void setGiven_name(String given_name) {
        this.given_name = given_name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSuburb() {
        return suburb;
    }

    public void setSuburb(String suburb) {
        this.suburb = suburb;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getStreet_number() {
        return street_number;
    }

    public void setStreet_number(String street_number) {
        this.street_number = street_number;
    }

    public String getAddress_1() {
        return address_1;
    }

    public void setAddress_1(String address_1) {
        this.address_1 = address_1;
    }

    public String getAddress_2() {
        return address_2;
    }

    public void setAddress_2(String address_2) {
        this.address_2 = address_2;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getSoc_sec_id() {
        return soc_sec_id;
    }

    public void setSoc_sec_id(String soc_sec_id) {
        this.soc_sec_id = soc_sec_id;
    }

    public String getBlocking_number() {
        return blocking_number;
    }

    public void setBlocking_number(String blocking_number) {
        this.blocking_number = blocking_number;
    }

    public String getFamily_role() {
        return family_role;
    }

    public void setFamily_role(String family_role) {
        this.family_role = family_role;
    }

    //根据序号返回属性值
    public String getAttributeByIndex(int index) {
        String result = new String();
        switch (index) {
            case 0 : {
                result = this.getRec_id();
                break;
            }
            case 1 : {
                result = this.getCulture();
                break;
            }
            case 2 : {
                result = this.getSex();
                break;
            }
            case 3 : {
                result = this.getAge();
                break;
            }
            case 4 : {
                result = this.getDate_of_birth();
                break;
            }
            case 5 : {
                result = this.getTitle();
                break;
            }

            case 6 : {
                result = this.getGiven_name();
                break;
            }

            case 7 : {
                result = this.getSurname();
                break;
            }

            case 8 : {
                result = this.getState();
                break;
            }

            case 9 : {
                result = this.getSuburb();
                break;
            }

            case 10 : {
                result = this.getPostcode();
                break;
            }

            case 11 : {
                result = this.getStreet_number();
                break;
            }

            case 12 : {
                result = this.getAddress_1();
                break;
            }

            case 13 : {
                result = this.getAddress_2();
                break;
            }

            case 14 : {
                result = this.getPhone_number();
                break;
            }

            case 15 : {
                result = this.getSoc_sec_id();
                break;
            }

            case 16 : {
                result = this.getBlocking_number();
                break;
            }

            case 17 : {
                result = this.getFamily_role();
                break;
            }

            default: {
                result = null;
                break;
            }
        }

        return result;
    }

}