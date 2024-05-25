package com.gayatri.store.controller;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.gayatri.store.models.Product;
import com.gayatri.store.models.ProductDto;
import com.gayatri.store.services.ProductRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
public class ProductController {
	
	@Autowired
	private ProductRepository repo;
	
	@GetMapping({"","/"})
	public String shoproductList(Model model) {
		List<Product> products=repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
		model.addAttribute("products", products);
		return "products/index";
	}
	
	@GetMapping("/create")
	public String showCreatePage(Model model) {
		ProductDto dto=new ProductDto();
		model.addAttribute("dto", dto);
		return "products/CreateProduct";
	}
	
	@PostMapping("/create")
	public String createProduct(@Valid @ModelAttribute("dto") ProductDto dto,BindingResult result) {
		if(dto.getImagefile().isEmpty()) {
			result.addError(new FieldError("dto", "imagefile", "Image field is required"));
		}
		
		if(result.hasErrors()) {
			return "products/CreateProduct";
		}
		
		//save image file
		MultipartFile image=dto.getImagefile();
		Date createat=new Date();
		String storageFileName=createat.getTime() + "_" + image.getOriginalFilename();
		
		try {
			String uploadDir="public/images/";
			Path uploadPath=Paths.get(uploadDir);
			
			if(!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}
			
			try(InputStream inputStream=image.getInputStream()){
				Files.copy(inputStream, Paths.get(uploadDir + storageFileName),StandardCopyOption.REPLACE_EXISTING);
			}
		}catch (Exception e) {
			
			System.out.println("Exception: "+ e.getMessage());
		}
		
		Product product=new Product();
		product.setName(dto.getName());
		product.setBrand(dto.getBrand());
		product.setCategory(dto.getCategory());
		product.setPrice(dto.getPrice());
		product.setDescription(dto.getDescription());
		product.setCreateAt(createat);
		product.setImageFileName(storageFileName);
		
		repo.save(product);
		
		return "redirect:/products";
	}
	
	@GetMapping("/edit")
	public String showEditPage(Model model, @RequestParam Integer id) {
		
		try {
			Product product=repo.findById(id).get();
			model.addAttribute("product", product);
			
			ProductDto dto=new ProductDto();
			dto.setName(product.getName());
			dto.setBrand(product.getBrand());
			dto.setCategory(product.getCategory());
			dto.setPrice(product.getPrice());
			dto.setDescription(product.getDescription());
			
			model.addAttribute("dto", dto);
			
		}catch(Exception e) {
			System.out.println("Exception: " + e.getMessage());
			return "redirect:/products";
		}
		
		return "products/EditProduct";
	}
	
	@PostMapping("/edit")
	public String updateProduct(Model model, @RequestParam int id, @Valid @ModelAttribute ProductDto dto, BindingResult result) {
		
		try {
			Product product= repo.findById(id).get();
			model.addAttribute("product", product);
			
			if(result.hasErrors()) {
				return  "products/EditProduct";
			}
			
			if(!dto.getImagefile().isEmpty()) {
				//delete previous image file
				String uploadDir="public/images/";
				Path prevImagePath= Paths.get(uploadDir + product.getImageFileName());
				
				try {
					Files.delete(prevImagePath);
				}catch(Exception e) {
					System.out.println("Exception :"+ e.getMessage());
				}
				
				//save new file
				MultipartFile img=dto.getImagefile();
				Date createat=new Date();
				String storageFileName=createat.getTime() + "_" + img.getOriginalFilename();
				
				try(InputStream inputStrem=img.getInputStream()){
					Files.copy(inputStrem, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
				}
				
				product.setImageFileName(storageFileName);
 			}
		}catch(Exception e) {
			System.out.println("Exception :"+ e.getMessage());
		}
		
		return "redirect:/products";
	}
	
	@GetMapping("/delete")
	public String delete(@RequestParam int id) {
		try {
			Product product=repo.findById(id).get();
			
			Path imgPath=Paths.get("public/images/" + product.getImageFileName());
			
			try {
				Files.delete(imgPath);
			}
			catch(Exception e) {
				System.out.println("Exception :"+ e.getMessage());
			}
			
			repo.delete(product);
			
		}catch(Exception e) {
			System.out.println("Exception :"+ e.getMessage());
		}
		
		return "redirect:/products";
	}

}
