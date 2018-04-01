/**
 * Created by Administrator on 2017/12/31.
 */
import java.util.*;

public class Print {
    public static void printPeople(People p) {
        System.out.println("rec_id:"+p.getRec_id()+"  culture:"+p.getCulture() + "  sex:"+p.getSex() + "  age:"+p.getAge()
                + "  date_of_birth:"+p.getDate_of_birth() + "  title:"+p.getTitle() + "  given_name:"+p.getGiven_name()
                + "  surname:"+p.getSurname() + "  state:"+p.getState() + "  suburb:"+p.getSuburb() + "  postcode:"+p.getPostcode()
                + "  street_number:"+p.getStreet_number() + "  address_1:"+p.getAddress_1() + "  address_2:"+p.getAddress_2()
                + "  phone_number:"+p.getPhone_number() + "  soc_sec_id:"+p.getSoc_sec_id());
//                + "  blocking_number:"+p.getBlocking_number() + "  family_role:"+p.getFamily_role());
    }
}
