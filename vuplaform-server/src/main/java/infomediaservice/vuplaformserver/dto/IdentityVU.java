package infomediaservice.vuplaformserver.dto;

import org.apache.commons.text.WordUtils;

import javax.validation.ValidationException;

public class IdentityVU {
    public static class Data{
        public boolean valid;
        public String profession;
        public String fatherName;
        public String address;
        public String gender;
        public String birthplace;
        public String educationLevel;
        public String deathDate;
        public String motherName;
        public String fullName;
        public String birthprovince;
        public String civilStatus;
        public String expirationDate;
    }

    public static class Person{
        public String names;
        public String lastNames;
        public String birthDate;
        public String number;
        public String nationality;
        public Data data;
    }

    public int code;
    public String message;
    public Person person;

    public IdentityVU(IdentityExperian identityExperian){

        code = 300;
        message = "Citizen information returned ok";
        Person person = new Person();
        IdentityExperian.Informe informe = identityExperian.informes.informe;

        Data data = new Data();
        person.birthDate = "Vacio";
        person.nationality = "Vacio";
        data.profession = "Vacio";
        data.fatherName = "Vacio";
        data.address = "Vacio";
        data.gender = "Vacio";
        data.birthplace = "Vacio";
        data.educationLevel = "Vacio";
        data.deathDate = "Vacio";
        data.motherName = "Vacio";
        data.birthprovince = "Vacio";
        data.civilStatus = "Vacio";
        data.expirationDate = "Vacio";
        data.fullName = "Vacio";
        person.names = "Vacio";
        person.lastNames = "Vacio";
        person.number = "Vacio";


        if(informe.naturalNacional!=null){
            person.names = WordUtils.capitalizeFully(informe.naturalNacional.nombres);
            person.lastNames = WordUtils.capitalizeFully(
                    (informe.naturalNacional.primerApellido+" "+informe.naturalNacional.segundoApellido).trim());
            person.number = Long.toString(informe.naturalNacional.identificacion.numero);

            data.fullName = WordUtils.capitalizeFully(person.names+" "+person.lastNames);

            if(informe.naturalNacional.identificacion.estado==null
                    ||informe.naturalNacional.identificacion.estado.length()==0){
                data.valid = true;
            }else{
                if(informe.naturalNacional.identificacion.estado.equals("00")){
                    data.valid = true;
                }else{
                    data.valid = false;
                }
            }
        }else if(informe.naturalExtranjera!=null){

            String[] fullname = informe.naturalExtranjera.nombre.split(" ");

            if(fullname.length==2){
                person.lastNames = WordUtils.capitalizeFully(fullname[0]);
                person.names = WordUtils.capitalizeFully(fullname[1]);
            }else if(fullname.length==3){
                person.lastNames = WordUtils.capitalizeFully(fullname[0]);
                person.names = WordUtils.capitalizeFully(fullname[1]+" "+fullname[2]);
            }else{
                person.lastNames = WordUtils.capitalizeFully(fullname[0]+" "+fullname[1]);
                StringBuilder stringBuilder = new StringBuilder();
                for(int i=2; i<fullname.length; i++){
                    if(stringBuilder.length()>0){
                        stringBuilder.append(" ");
                    }
                    stringBuilder.append(fullname[i]);
                }
                person.names = WordUtils.capitalizeFully(stringBuilder.toString());
            }

            try {
                person.number = Long.toString(Long.parseLong(informe.naturalExtranjera.numero));
            }catch (Exception ignore){
                person.number = informe.naturalExtranjera.numero;
            }

            person.nationality = WordUtils.capitalizeFully(informe.naturalExtranjera.nacionalidad);
            data.fullName = WordUtils.capitalizeFully(person.names+" "+person.lastNames);
            data.valid = true;
        }else{
            throw new ValidationException("The response could not be processed: Person data not found");
        }

        person.data = data;
        this.person = person;
    }
}
