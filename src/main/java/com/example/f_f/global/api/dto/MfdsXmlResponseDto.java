package com.example.f_f.global.api.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@XmlRootElement(name = "response")  // XML 루트 노드에 맞게
public class MfdsXmlResponseDto {

    @XmlElement(name = "body")
    private Body body;

    @Getter
    @Setter
    public static class Body {
        @XmlElement(name = "items")
        private Items items;
    }

    @Getter
    @Setter
    public static class Items {
        @XmlElement(name = "item")
        private List<ItemDto> itemList;
    }
}