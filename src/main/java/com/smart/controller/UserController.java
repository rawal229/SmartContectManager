package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.razorpay.*;

import com.smart.dao.ContectRepo;
import com.smart.dao.MyOrderRepo;
import com.smart.dao.UserRepo;
import com.smart.entities.Contect;
import com.smart.entities.MyOrder;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ContectRepo contectRepo;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private MyOrderRepo myOrderRepo;

    // Method for adding common data to response
    @ModelAttribute
    public void addCommonData(Model model, Principal principal) {
        String userName = principal.getName();
        System.out.println("USERNAME : " + userName);

        // Get the user using username(Email)

        User user = this.userRepo.getUserByUserName(userName);
        System.out.println("USER : " + user);

        model.addAttribute("user", user);

    }

    // dashbord home
    @RequestMapping("/index")
    public String dashbord(Model model) {
        model.addAttribute("title", "User Dashbord");
        return "normal/user_dashbord";
    }

    // open add form handler
    @GetMapping("/add-contect")
    public String addConcectFrom(Model m) {

        m.addAttribute("title", "Add Contect");
        m.addAttribute("contect", new Contect());

        return "normal/add_contect_form";
    }

    // data send from to database
    // @PostMapping("/process.contect")
    // public String processData(@ModelAttribute Contect contect) {
    // System.out.println("Data : " + contect);
    // return "normal/add_contect_form";
    // }

    // @PostMapping("/process.contect")
    @PostMapping("/process-contect")
    public String processData(@Valid @ModelAttribute("contect") Contect contect,
            BindingResult result,
            Model model,
            Principal principal, @RequestParam("image") MultipartFile file, HttpSession session) {
        try {
            // Get the user name to the contact
            String name = principal.getName();
            User user = this.userRepo.getUserByUserName(name);

            // Image add in database -> processing and uploading file image
            if (file.isEmpty()) {
                // if the file is empty then try our message
                System.out.println("File is empty...");
                contect.setImage("contact.png");

            } else {
                // upload the file to folder and update the name to contact
                contect.setImage(file.getOriginalFilename());

                File saveFile = new ClassPathResource("static/image").getFile();

                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                System.out.println("Image is uploaded...");

            }

            // IMP lines to ByDirection conncetion - start
            contect.setUser(user);
            user.getContects().add(contect);
            // IMP lines to ByDirection conncetion - end

            this.userRepo.save(user);

            System.out.println("Data : " + contect);
            System.out.println("Added to database....");

            // message success....
            session.setAttribute("message", new Message("Your contact is added !! Add more...", "success"));

        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
            e.printStackTrace();
            // message error....
            session.setAttribute("message", new Message("Contact is not added !! try again...", "danger"));

        }
        return "normal/add_contect_form";
    }

    // show contact handler
    // per page - 5[n]
    // current page - 0[page]
    @GetMapping("/show-contacts/{page}")
    public String showContact(@PathVariable("page") Integer page, Model model, Principal principal) {
        model.addAttribute("title", "Show User Contact");

        String userName = principal.getName();

        User user = this.userRepo.getUserByUserName(userName);

        // pagination
        // currentPage - page
        // Contact Per Page - 5
        Pageable pageable = PageRequest.of(page, 5);

        Page<Contect> contects = this.contectRepo.findContectByUser(user.getId(), pageable);

        model.addAttribute("contects", contects);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", contects.getTotalPages());

        return "normal/show_contacts";
    }

    // showing particular contact details
    @RequestMapping("/{cId}/contect")
    public String showContectDetails(@PathVariable("cId") Integer cId, Model model, Principal principal) {
        System.out.println("CID : " + cId);

        Optional<Contect> contectOptional = this.contectRepo.findById(cId);
        Contect contect = contectOptional.get();

        //
        String userName = principal.getName();
        User user = this.userRepo.getUserByUserName(userName);

        if (user.getId() == contect.getUser().getId()) {
            model.addAttribute("contect", contect);
            model.addAttribute("title", contect.getName());
        }

        return "normal/contact_details";
    }

    // Delete Contect handler
    @GetMapping("/delete/{cId}")
    public String deleteContect(@PathVariable("cId") Integer cId, Model model, HttpSession session,
            Principal principal) {
        System.out.println("CID : " + cId);
        // Optional<Contect> contectOptional = this.contectRepo.findById(cId);
        // Contect contect = contectOptional.get();
        Contect contect = this.contectRepo.findById(cId).get();

        // check..Assignment
        System.out.println("Contact : " + contect.getcId());

        // to unlink user to contect
        User user = this.userRepo.getUserByUserName(principal.getName());
        user.getContects().remove(contect);

        this.userRepo.save(user);

        System.out.println("DELETED");
        session.setAttribute("message", new Message("Contact deleted successfully..", "success"));

        return "redirect:/user/show-contacts/0";

    }

    // Open Update contact handler
    @PostMapping("/update-contect/{cid}")
    public String updateForm(@PathVariable("cid") Integer cId, Model model) {
        model.addAttribute("title", "Update Contact");

        Contect contect = this.contectRepo.findById(cId).get();

        model.addAttribute("contect", contect);

        return "normal/update_form";
    }

    // Update Contact handler
    @PostMapping("/process-update")
    public String updateHandler(@ModelAttribute Contect contect, BindingResult result,
            @RequestParam("image") MultipartFile file, Model m,
            HttpSession session, Principal principal) {

        try {
            Contect oldContectDetail = this.contectRepo.findById(contect.getcId()).get();
            // image
            if (!file.isEmpty()) {
                // file work..
                // rewrite

                // delete old photo
                File deleteFile = new ClassPathResource("static/image").getFile();
                File file1 = new File(deleteFile, oldContectDetail.getImage());
                file1.delete();

                // update new photo
                File saveFile = new ClassPathResource("static/image").getFile();

                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                contect.setImage(file.getOriginalFilename());

                // System.out.println("Image updated successfully...");

            } else {
                contect.setImage(oldContectDetail.getImage());
            }

            User user = this.userRepo.getUserByUserName(principal.getName());

            contect.setUser(user);

            this.contectRepo.save(contect);

            session.setAttribute("message", new Message("Your Contact is updated...", "success"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("CONTACT NAME : " + contect.getName());
        System.out.println("CONTACT ID : " + contect.getcId());
        return "redirect:/user/" + contect.getcId() + "/contect";
    }

    // User profile handler
    @GetMapping("/profile")
    public String userProfile(Model m) {
        m.addAttribute("title", "Profile Page");
        return "normal/profile";
    }

    // open settings handler
    @GetMapping("/settings")
    public String opneSetings() {
        return "normal/settings";
    }

    // change password handler
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword, Principal principal, HttpSession session) {
        System.out.println("OLD PASSWORD : " + oldPassword + "\nNEW PASSWORD : " + newPassword);
        // fatch the old password
        String userName = principal.getName();
        User currentUser = this.userRepo.getUserByUserName(userName);
        System.out.println(currentUser.getPassword());

        // to check old row(normal) and old encoded password
        if (this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
            // change the password
            currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
            this.userRepo.save(currentUser);
            session.setAttribute("message", new Message("Password successfully changed", "success"));
        } else {
            // error...
            session.setAttribute("message", new Message("Please enter currect old password !!", "error"));
            return "normal/settings";
        }
        return "normal/user_dashbord";
        // return "redirect:/normal/user_dashbord";
    }

    // creating order for payment
    @PostMapping("/create_order")
    @ResponseBody
    public String createOrder(@RequestBody Map<String, Object> data, Principal principal) throws Exception {
        System.out.println("Inside order function handler..");
        System.out.println("Amout is : " + data);
        // to convert amount string to integer
        int amt = Integer.parseInt(data.get("amount").toString());

        var client = new RazorpayClient("rzp_test_4oE1hGCHrTghvY", "93a5Pte2kt68BDFUGnPUVPJP");

        JSONObject ob = new JSONObject();
        ob.put("amount", amt * 100);
        ob.put("currency", "INR");
        ob.put("receipt", "txn_62532");

        // creating new order
        Order order = client.orders.create(ob);
        System.out.println(order);

        // save order in database
        MyOrder myOrder = new MyOrder();
        myOrder.setAmount(order.get("amount") + "");
        myOrder.setOrderId(order.get("id"));
        myOrder.setStatus("created");
        myOrder.setPaymentId(null);
        myOrder.setReceipt(order.get("receipt"));
        myOrder.setUser(this.userRepo.getUserByUserName(principal.getName()));
        this.myOrderRepo.save(myOrder);

        return order.toString();
    }

    // update payment on server handler
    @PostMapping("/update_order")
    public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object>data){

        MyOrder myOrder = this.myOrderRepo.findByOrderId(data.get("order_id").toString());

        myOrder.setPaymentId(data.get("payment_id").toString());
        myOrder.setStatus(data.get("status").toString());
        this.myOrderRepo.save(myOrder);

        System.out.println(data);
        return ResponseEntity.ok(Map.of("msg", "updated"));
    }
}
