from fastapi import APIRouter, Depends, HTTPException
from models import User, Cart, CartItem, Product
from authentication import get_current_user

router = APIRouter()


@router.post("/cart/add")
async def add_to_cart(
    product_id: int, quantity: int, user: User = Depends(get_current_user)
):
    product = await Product.get(id=product_id)
    cart, _ = await Cart.get_or_create(user=user)
    cart_item, created = await CartItem.get_or_create(cart=cart, product=product)

    if not created:
        cart_item.quantity += quantity
    else:
        cart_item.quantity = quantity

    cart_item.price = product.new_price
    await cart_item.save()
    return {"message": "Item added to cart"}


@router.get("/cart")
async def view_cart(user: User = Depends(get_current_user)):
    cart, _ = await Cart.get_or_create(user=user)
    items = await CartItem.filter(cart=cart).prefetch_related("product")
    total = sum(item.quantity * item.price for item in items)
    return {
        "items": [
            {
                "product": item.product.name,
                "quantity": item.quantity,
                "price": item.price,
                "total": item.quantity * item.price,
            }
            for item in items
        ],
        "total": total,
    }


@router.put("/cart/update/{item_id}")
async def update_cart_item(
    item_id: int, quantity: int, user: User = Depends(get_current_user)
):
    cart = await Cart.get(user=user)
    item = await CartItem.get(id=item_id, cart=cart)
    item.quantity = quantity
    await item.save()
    return {"message": "Cart item updated"}


@router.delete("/cart/remove/{item_id}")
async def remove_from_cart(item_id: int, user: User = Depends(get_current_user)):
    cart = await Cart.get(user=user)
    await CartItem.filter(id=item_id, cart=cart).delete()
    return {"message": "Item removed from cart"}
