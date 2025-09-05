package services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.data.Form;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;

public class GeneralService {

    public static <E> ObjectNode extractWishAssociationRequestFromForm(Form<E> form) {
        List<Long> selectedWishIds = new ArrayList<>();
        int wishIndex = 0;
        String fieldName = "selectedWishIds[" + wishIndex + "]";
        while (form.field(fieldName).getValue().isPresent()) {
            selectedWishIds.add(Long.parseLong(form.field(fieldName).getValue().get()));
            wishIndex++;
            fieldName = "selectedWishIds[" + wishIndex + "]";
        }
        ObjectMapper wishAssociationRequestMapper = new ObjectMapper();
        ObjectNode wishAssociationRequest = wishAssociationRequestMapper.createObjectNode();
        wishAssociationRequest.put("wishes", Json.toJson(selectedWishIds));
        return wishAssociationRequest;
    }
}
