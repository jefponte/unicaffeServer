package br.edu.unilab.unicaffe.api;


import java.nio.charset.Charset;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

import br.edu.unilab.unicaffe.model.Usuario;


public class Resource extends RestTemplate{
	
	//"https://catracahomologacao.unilab.edu.br/api/v2/"
	public static final String URL = "https://api.unilab.edu.br/api/";
	
	
	public static HttpHeaders getHeaders(){
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(new MediaType("application", "json", Charset.forName("utf-8")));
		return headers;
	}
	
	public ResponseEntity<String> getResponse(String get){
		HttpEntity<String> request = new HttpEntity<String>(getHeaders());
		return exchange(URL+get, HttpMethod.POST, request, String.class);
	}
	
	public int authenticate(Usuario usuario) {
		try {
			ResponseEntity<String> response = this.getResponse("authenticate_md5?"+"login="+usuario.getLogin()+"&senha="+usuario.getSenha());
			if(response.getStatusCode() != HttpStatus.OK) {
				System.err.println("Sem comunicação com servidor!");
				return 0;
			}	
			String output = response.getBody();
			Gson gson = new Gson();
			Usuario obj = gson.fromJson(output, Usuario.class);
			usuario.setId(obj.getId());
			usuario.setNome(obj.getName());
			return obj.getId();	
		}catch(Exception e) {
			System.out.println("Excessão capturada");
			return 0;
		}
		
	}

}