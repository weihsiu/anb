package anb

object Orders {
  case class Customer(id: String, name: String, address: String)
  case class Supplier(id: String, name: String, email: String)
  case class Product(id: String, name: String, supplierId: String)
  case class Order(id: String, customerId: String, productIds: List[String])

  case class FullProduct(id: String, name: String, supplier: Supplier)
  case class FullOrder(id: String, customer: Customer, products: List[FullProduct])
}
